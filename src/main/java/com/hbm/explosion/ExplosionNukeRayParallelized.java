package com.hbm.explosion;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.interfaces.IExplosionRay;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.SetSubChunkAirPacket;
import com.hbm.util.ConcurrentBitSet;
import com.hbm.util.SubChunkKey;
import com.hbm.util.SubChunkSnapshot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Threaded DDA raytracer for mk5 explosion.
 *
 * @author mlbv
 */
public class ExplosionNukeRayParallelized implements IExplosionRay {

    /**
     * If true, will read blockstates off the main thread.
     */
    private static final boolean unsafeAccess = true;
    private static final int WORLD_HEIGHT = 256;
    private static final int BITSET_SIZE = 16 * WORLD_HEIGHT * 16;
    private static final int SUBCHUNK_PER_CHUNK = WORLD_HEIGHT >> 4;
    private static final float NUKE_RESISTANCE_CUTOFF = 2_000_000F;
    private static final float INITIAL_ENERGY_FACTOR = 0.3F; // Scales crater, no impact on performance
    private static final double RESOLUTION_FACTOR = 1.0;  // Scales ray density, no impact on crater radius
    private static final int LUT_RESISTANCE_BINS = 256;
    private static final int LUT_DISTANCE_BINS = 256;
    private static final float LUT_MAX_RESISTANCE = 100.0F;
    private static final float[][] ENERGY_LOSS_LUT = new float[LUT_RESISTANCE_BINS][LUT_DISTANCE_BINS];
    private static final String TAG_ALGORITHM = "algorithm";
    private static final String TAG_RAY_COUNT = "rayCount";
    private static final String TAG_IS_CONTAINED = "isContained";
    private static final String TAG_COLLECT_FINISHED = "collectDone";
    private static final String TAG_CONSOLIDATE_FINISHED = "consolidateDone";
    private static final String TAG_DESTROY_FINISHED = "destroyDone";
    private static final String TAG_DESTRUCTION_MAP = "destructionMap";
    private static final String TAG_ORDERED_CHUNKS = "orderedChunks";
    private static final String TAG_DESTROYED_LIST = "destroyedList";
    private static final String TAG_CHUNK_X = "cX";
    private static final String TAG_CHUNK_Z = "cZ";
    private static final String TAG_BITSET = "bitset";
    private static final String TAG_POS_X = "pX";
    private static final String TAG_POS_Y = "pY";
    private static final String TAG_POS_Z = "pZ";

    static {
        for (int r = 0; r < LUT_RESISTANCE_BINS; r++) {
            float resistance = (r / (float) (LUT_RESISTANCE_BINS - 1)) * LUT_MAX_RESISTANCE;
            for (int d = 0; d < LUT_DISTANCE_BINS; d++) {
                float distFrac = d / (float) (LUT_DISTANCE_BINS - 1);
                ENERGY_LOSS_LUT[r][d] = (float) (Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0);
            }
        }
    }

    private final World world;
    private final double explosionX, explosionY, explosionZ;
    private final int originX, originY, originZ;
    private final int radius;
    private final ConcurrentMap<ChunkPos, ConcurrentBitSet> destructionMap;
    private final ConcurrentMap<ChunkPos, ConcurrentMap<Integer, DoubleAdder>> damageMap;
    private final ConcurrentMap<SubChunkKey, SubChunkSnapshot> snapshots;
    private final ConcurrentMap<SubChunkKey, ConcurrentLinkedQueue<RayTracerTask>> waitingRoom;
    private final List<ChunkPos> orderedChunks;
    private final BlockingQueue<SubChunkKey> reactiveSnapQueue;
    private final Iterator<SubChunkKey> proactiveSnapIterator;
    private final int strength;
    private final CompletableFuture<List<Vec3d>> directionsFuture;
    private List<BlockPos> destroyedList;
    private int algorithm;
    private int rayCount;
    private ForkJoinPool pool;
    private volatile List<Vec3d> directions;
    private volatile boolean collectFinished = false;
    private volatile boolean consolidationFinished = false;
    private volatile boolean destroyFinished = false;
    private volatile boolean isContained = true;

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius, int algorithm) {
        this(world, x, y, z, strength, radius);
        this.algorithm = algorithm;
        initializeAndStartWorkers();
    }

    public ExplosionNukeRayParallelized(World world, double x, double y, double z, int strength, int radius) {
        this.world = world;
        this.explosionX = x;
        this.explosionY = y;
        this.explosionZ = z;
        this.originX = (int) Math.floor(x);
        this.originY = (int) Math.floor(y);
        this.originZ = (int) Math.floor(z);
        this.strength = strength;
        this.radius = radius;

        if (!CompatibilityConfig.isWarDim(world)) {
            this.collectFinished = true;
            this.consolidationFinished = true;
            this.destroyFinished = true;
            this.isContained = true;

            this.proactiveSnapIterator = Collections.emptyIterator();
            this.reactiveSnapQueue = new LinkedBlockingQueue<>();
            this.destructionMap = new ConcurrentHashMap<>();
            this.damageMap = new ConcurrentHashMap<>();
            this.snapshots = new ConcurrentHashMap<>();
            this.waitingRoom = new ConcurrentHashMap<>();
            this.orderedChunks = new ArrayList<>();
            this.destroyedList = new ArrayList<>();
            this.directionsFuture = CompletableFuture.completedFuture(Collections.emptyList());
            this.pool = null;
            return;
        }

        this.rayCount = Math.max(0, (int) (2.5 * Math.PI * strength * strength * RESOLUTION_FACTOR));
        List<SubChunkKey> sortedSubChunks = getAllSubChunks();
        this.proactiveSnapIterator = sortedSubChunks.iterator();
        this.reactiveSnapQueue = new LinkedBlockingQueue<>();

        int initialChunkCapacity = (int) sortedSubChunks.stream().map(SubChunkKey::getPos).distinct().count();
        this.destructionMap = new ConcurrentHashMap<>(initialChunkCapacity);
        this.damageMap = new ConcurrentHashMap<>(initialChunkCapacity);

        int subChunkCount = sortedSubChunks.size();
        this.snapshots = new ConcurrentHashMap<>(subChunkCount);
        this.waitingRoom = new ConcurrentHashMap<>(subChunkCount);
        this.orderedChunks = new ArrayList<>();
        this.destroyedList = new ArrayList<>();

        this.directionsFuture = CompletableFuture.supplyAsync(() -> generateSphereRays(rayCount));
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    private static float getNukeResistance(Block b) {
        if (b.getDefaultState().getMaterial().isLiquid()) return 0.1F;
        if (b == Blocks.SANDSTONE) return 4.0F;
        if (b == Blocks.OBSIDIAN) return 18.0F;
        return b.getExplosionResistance(null);
    }

    private void initializeAndStartWorkers() {
        if (rayCount <= 0) {
            this.collectFinished = true;
            this.consolidationFinished = true;
            return;
        }

        int workers = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        this.pool = new ForkJoinPool(workers);

        RayTracerTask mainTask = new RayTracerTask(0, rayCount);

        CompletableFuture.runAsync(() -> pool.invoke(mainTask), pool).whenComplete((v, ex) -> {
            if (ex != null) {
                MainRegistry.logger.error("Nuke ray-tracing failed catastrophically.", ex);
            }
            collectFinished = true;
            if (algorithm == 2) {
                pool.submit(this::runConsolidation);
            } else {
                consolidationFinished = true;
            }
        });
    }

    private List<SubChunkKey> getAllSubChunks() {
        List<SubChunkKey> keys = new ArrayList<>();
        int cr = (radius + 15) >> 4;
        int minCX = (originX >> 4) - cr;
        int maxCX = (originX >> 4) + cr;
        int minCZ = (originZ >> 4) - cr;
        int maxCZ = (originZ >> 4) + cr;
        int minSubY = Math.max(0, (originY - radius) >> 4);
        int maxSubY = Math.min(SUBCHUNK_PER_CHUNK - 1, (originY + radius) >> 4);
        int originSubY = originY >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                for (int subY = minSubY; subY <= maxSubY; subY++) {
                    int chunkCenterX = (cx << 4) + 8;
                    int chunkCenterY = (subY << 4) + 8;
                    int chunkCenterZ = (cz << 4) + 8;
                    double dx = chunkCenterX - explosionX;
                    double dy = chunkCenterY - explosionY;
                    double dz = chunkCenterZ - explosionZ;
                    if (dx * dx + dy * dy + dz * dz <= (radius + 14) * (radius + 14)) { // +14 for margin of error
                        keys.add(new SubChunkKey(cx, cz, subY));
                    }
                }
            }
        }
        keys.sort(Comparator.comparingInt(key -> {
            int distCX = key.getChunkXPos() - (originX >> 4);
            int distCZ = key.getChunkZPos() - (originZ >> 4);
            int distSubY = key.getSubY() - originSubY;
            return distCX * distCX + distCZ * distCZ + distSubY * distSubY;
        }));
        return keys;
    }

    @Override
    public void cacheChunksTick(int timeBudgetMs) {
        if (unsafeAccess || collectFinished) return;
        final long deadline = System.nanoTime() + (timeBudgetMs * 1_000_000L);
        while (System.nanoTime() < deadline) {
            SubChunkKey ck = reactiveSnapQueue.poll();
            if (ck == null) break;
            processCacheKey(ck);
        }
        while (System.nanoTime() < deadline && proactiveSnapIterator.hasNext()) {
            SubChunkKey ck = proactiveSnapIterator.next();
            processCacheKey(ck);
        }
    }

    private void processCacheKey(SubChunkKey ck) {
        if (snapshots.containsKey(ck)) return;
        snapshots.put(ck, SubChunkSnapshot.getSnapshot(world, ck, BombConfig.chunkloading));
        ConcurrentLinkedQueue<RayTracerTask> waiters = waitingRoom.remove(ck);
        if (waiters != null && pool != null && !pool.isShutdown()) {
            for (RayTracerTask task : waiters) {
                pool.submit(task);
            }
        }
    }

    @Override
    public void destructionTick(int timeBudgetMs) {
        if (!collectFinished || !consolidationFinished || destroyFinished) return;

        final long deadline = System.nanoTime() + timeBudgetMs * 1_000_000L;

        if (orderedChunks.isEmpty() && !destructionMap.isEmpty()) {
            orderedChunks.addAll(destructionMap.keySet());
            orderedChunks.sort(Comparator.comparingInt(c -> Math.abs((originX >> 4) - c.x) + Math.abs((originZ >> 4) - c.z)));
        }

        Iterator<ChunkPos> it = orderedChunks.iterator();
        while (it.hasNext() && System.nanoTime() < deadline) {
            ChunkPos cp = it.next();
            ConcurrentBitSet bs = destructionMap.get(cp);
            if (bs == null) {
                it.remove();
                continue;
            }

            Chunk chunk = world.getChunk(cp.x, cp.z);
            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
            boolean chunkModified = false;

            for (int subY = 0; subY < SUBCHUNK_PER_CHUNK; subY++) {
                ExtendedBlockStorage storage = storages[subY];
                if (storage == null || storage.isEmpty()) continue;

                int startBit = (WORLD_HEIGHT - 1 - ((subY << 4) + 15)) << 8;
                int endBit = ((WORLD_HEIGHT - 1 - (subY << 4)) << 8) | 0xFF;

                boolean hasSurvivor = false;
                int clearBit = bs.nextClearBit(startBit);
                while (clearBit >= 0 && clearBit <= endBit) {
                    int yGlobal = WORLD_HEIGHT - 1 - (clearBit >>> 8);
                    int lx = (clearBit >>> 4) & 0xF;
                    int lz = clearBit & 0xF;
                    int ly = yGlobal - (subY << 4);
                    if (storage.get(lx, ly, lz).getBlock() != Blocks.AIR) {
                        hasSurvivor = true;
                        break;
                    }
                    clearBit = bs.nextClearBit(clearBit + 1);
                }

                if (!hasSurvivor) {
                    chunkModified = true;
                    IBlockState airState = Blocks.AIR.getDefaultState();
                    for (int lx = 0; lx < 16; lx++) {
                        for (int ly = 0; ly < 16; ly++) {
                            for (int lz = 0; lz < 16; lz++) {
                                int xGlobal = (cp.x << 4) + lx;
                                int yGlobal = (subY << 4) + ly;
                                int zGlobal = (cp.z << 4) + lz;
                                BlockPos pos = new BlockPos(xGlobal, yGlobal, zGlobal);
                                world.removeTileEntity(pos);
                                if (storage.get(lx, ly, lz).getBlock() != Blocks.AIR) {
                                    destroyedList.add(pos);
                                }
                                storage.set(lx, ly, lz, airState);
                            }
                        }
                    }
                    storage.recalculateRefCounts();
                    bs.clear(startBit, endBit + 1);

                    double sendRange = this.radius * 2.0;
                    NetworkRegistry.TargetPoint target = new NetworkRegistry.TargetPoint(world.provider.getDimension(), explosionX, explosionY,
                            explosionZ, sendRange);
                    PacketDispatcher.wrapper.sendToAllAround(new SetSubChunkAirPacket(cp.x, cp.z, subY), target);

                    continue;
                }

                int bit = bs.nextSetBit(startBit);

                while (bit >= 0 && bit <= endBit) {
                    if (System.nanoTime() >= deadline) {
                        if (chunkModified) chunk.markDirty();
                        return;
                    }
                    int yGlobal = WORLD_HEIGHT - 1 - (bit >>> 8);
                    int xGlobal = (cp.x << 4) | ((bit >>> 4) & 0xF);
                    int zGlobal = (cp.z << 4) | (bit & 0xF);
                    int xLocal = xGlobal & 0xF;
                    int yLocal = yGlobal & 0xF;
                    int zLocal = zGlobal & 0xF;

                    IBlockState oldState = storage.get(xLocal, yLocal, zLocal);
                    if (oldState.getBlock() != Blocks.AIR) {
                        BlockPos pos = new BlockPos(xGlobal, yGlobal, zGlobal);
                        world.removeTileEntity(pos);
                        storage.set(xLocal, yLocal, zLocal, Blocks.AIR.getDefaultState());
                        chunkModified = true;
                        world.notifyBlockUpdate(pos, oldState, Blocks.AIR.getDefaultState(), 3);
                        this.destroyedList.add(pos);
                    }
                    bs.clear(bit);
                    bit = bs.nextSetBit(bit + 1);
                }
            }
            if (chunkModified) chunk.markDirty();
            if (bs.isEmpty()) {
                destructionMap.remove(cp);
                for (int subY = 0; subY < SUBCHUNK_PER_CHUNK; subY++) snapshots.remove(new SubChunkKey(cp, subY));
                it.remove();
            }
        }
        if (orderedChunks.isEmpty() && destructionMap.isEmpty()) {
            secondPass();
            destroyFinished = true;
            if (pool != null) pool.shutdown();
        }
    }

    private void secondPass() {
        if (destroyedList == null || destroyedList.isEmpty()) return;
        Set<ChunkPos> modifiedChunks = new HashSet<>();
        for (BlockPos pos : destroyedList) {
            world.notifyNeighborsOfStateChange(pos, Blocks.AIR, true);
            world.checkLightFor(EnumSkyBlock.SKY, pos);
            world.checkLightFor(EnumSkyBlock.BLOCK, pos);
            modifiedChunks.add(new ChunkPos(pos));
        }
        for (ChunkPos cp : modifiedChunks) {
            world.markBlockRangeForRenderUpdate(cp.x << 4, 0, cp.z << 4, (cp.x << 4) | 15, WORLD_HEIGHT - 1, (cp.z << 4) | 15);
        }
        destroyedList.clear();
        destroyedList = null;
    }


    @Override
    public boolean isComplete() {
        return collectFinished && consolidationFinished && destroyFinished;
    }

    @Override
    public boolean isContained() {
        return isContained;
    }

    @Override
    public void cancel() {
        this.collectFinished = true;
        this.consolidationFinished = true;
        this.destroyFinished = true;

        if (this.waitingRoom != null) this.waitingRoom.clear();
        if (this.destroyedList != null) this.destroyedList.clear();

        if (this.pool != null && !this.pool.isShutdown()) {
            this.pool.shutdownNow();
            try {
                if (!this.pool.awaitTermination(100, TimeUnit.MILLISECONDS))
                    MainRegistry.logger.error("ExplosionNukeRayParallelized ForkJoinPool did not terminate promptly on cancel.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!this.pool.isShutdown()) this.pool.shutdownNow();
            }
        }
        if (this.destructionMap != null) this.destructionMap.clear();
        if (this.damageMap != null) this.damageMap.clear();
        if (this.snapshots != null) this.snapshots.clear();
        if (this.orderedChunks != null) this.orderedChunks.clear();
    }

    private List<Vec3d> generateSphereRays(int count) {
        List<Vec3d> list = new ArrayList<>(count);
        if (count == 0) return list;
        if (count == 1) {
            list.add(new Vec3d(1, 0, 0));
            return list;
        }
        double phi = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < count; i++) {
            double y = 1.0 - (i / (double) (count - 1)) * 2.0;
            double r = Math.sqrt(1.0 - y * y);
            double t = phi * i;
            list.add(new Vec3d(Math.cos(t) * r, y, Math.sin(t) * r));
        }
        return list;
    }

    private void runConsolidation() {
        damageMap.forEach((cp, innerDamageMap) -> {
            if (innerDamageMap.isEmpty()) {
                damageMap.remove(cp);
                return;
            }
            ConcurrentBitSet chunkDestructionBitSet = destructionMap.computeIfAbsent(cp, k -> new ConcurrentBitSet(BITSET_SIZE));

            Iterator<Map.Entry<Integer, DoubleAdder>> iterator = innerDamageMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, DoubleAdder> entry = iterator.next();
                int bitIndex = entry.getKey();
                DoubleAdder accumulatedDamageAdder = entry.getValue();

                float accumulatedDamage = (float) accumulatedDamageAdder.sum();
                if (accumulatedDamage <= 0.0f) {
                    iterator.remove();
                    continue;
                }
                int yGlobal = WORLD_HEIGHT - 1 - (bitIndex >>> 8);

                Block originalBlock;
                if (unsafeAccess) {
                    int xGlobal = (cp.x << 4) | ((bitIndex >>> 4) & 0xF);
                    int zGlobal = (cp.z << 4) | (bitIndex & 0xF);
                    originalBlock = world.getBlockState(new BlockPos(xGlobal, yGlobal, zGlobal)).getBlock();
                } else {
                    int subY = yGlobal >> 4;
                    if (subY < 0) {
                        iterator.remove();
                        continue;
                    }
                    SubChunkKey snapshotKey = new SubChunkKey(cp, subY);
                    SubChunkSnapshot snap = snapshots.get(snapshotKey);
                    if (snap == null || snap == SubChunkSnapshot.EMPTY) {
                        iterator.remove();
                        continue;
                    }
                    int xLocal = (bitIndex >>> 4) & 0xF;
                    int zLocal = bitIndex & 0xF;
                    originalBlock = snap.getBlock(xLocal, yGlobal & 0xF, zLocal);
                }

                if (originalBlock == Blocks.AIR) {
                    iterator.remove();
                    continue;
                }
                float resistance = getNukeResistance(originalBlock);
                if (accumulatedDamage >= resistance * RESOLUTION_FACTOR) {
                    chunkDestructionBitSet.set(bitIndex);
                }
                iterator.remove();
            }
            if (innerDamageMap.isEmpty()) damageMap.remove(cp);
        });
        damageMap.clear();
        consolidationFinished = true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        this.algorithm = nbt.getInteger(TAG_ALGORITHM);
        this.rayCount = nbt.getInteger(TAG_RAY_COUNT);
        this.isContained = nbt.getBoolean(TAG_IS_CONTAINED);
        this.collectFinished = nbt.getBoolean(TAG_COLLECT_FINISHED);
        this.consolidationFinished = nbt.getBoolean(TAG_CONSOLIDATE_FINISHED);
        this.destroyFinished = nbt.getBoolean(TAG_DESTROY_FINISHED);

        if (collectFinished && consolidationFinished && !destroyFinished) {
            if (nbt.hasKey(TAG_DESTRUCTION_MAP, Constants.NBT.TAG_LIST)) {
                NBTTagList list = nbt.getTagList(TAG_DESTRUCTION_MAP, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound tag = list.getCompoundTagAt(i);
                    ChunkPos pos = new ChunkPos(tag.getInteger(TAG_CHUNK_X), tag.getInteger(TAG_CHUNK_Z));
                    long[] bitsetData = ((NBTTagLongArray) tag.getTag(TAG_BITSET)).data;
                    this.destructionMap.put(pos, ConcurrentBitSet.fromLongArray(bitsetData, BITSET_SIZE));
                }
            }
            if (nbt.hasKey(TAG_ORDERED_CHUNKS, Constants.NBT.TAG_LIST)) {
                NBTTagList list = nbt.getTagList(TAG_ORDERED_CHUNKS, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound nbt1 = list.getCompoundTagAt(i);
                    this.orderedChunks.add(new ChunkPos(nbt1.getInteger(TAG_CHUNK_X), nbt1.getInteger(TAG_CHUNK_Z)));
                }
            }
            if (nbt.hasKey(TAG_DESTROYED_LIST, Constants.NBT.TAG_LIST)) {
                NBTTagList list = nbt.getTagList(TAG_DESTROYED_LIST, Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound nbt1 = list.getCompoundTagAt(i);
                    this.destroyedList.add(new BlockPos(nbt1.getInteger(TAG_POS_X), nbt1.getInteger(TAG_POS_Y), nbt1.getInteger(TAG_POS_Z)));
                }
            }
        } else if (!collectFinished || !consolidationFinished) {
            if (!CompatibilityConfig.isWarDim(world)) {
                this.collectFinished = this.consolidationFinished = this.destroyFinished = true;
                return;
            }
            initializeAndStartWorkers();
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        if (!BombConfig.enableNukeNBTSaving) return;
        nbt.setInteger(TAG_ALGORITHM, this.algorithm);
        nbt.setInteger(TAG_RAY_COUNT, this.rayCount);
        nbt.setBoolean(TAG_IS_CONTAINED, this.isContained);
        nbt.setBoolean(TAG_COLLECT_FINISHED, this.collectFinished);
        nbt.setBoolean(TAG_CONSOLIDATE_FINISHED, this.consolidationFinished);
        nbt.setBoolean(TAG_DESTROY_FINISHED, this.destroyFinished);

        if (!this.collectFinished || !this.consolidationFinished || this.destroyFinished) return;
        if (!this.destructionMap.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (Map.Entry<ChunkPos, ConcurrentBitSet> entry : this.destructionMap.entrySet()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger(TAG_CHUNK_X, entry.getKey().x);
                tag.setInteger(TAG_CHUNK_Z, entry.getKey().z);
                tag.setTag(TAG_BITSET, new NBTTagLongArray(entry.getValue().toLongArray()));
                list.appendTag(tag);
            }
            nbt.setTag(TAG_DESTRUCTION_MAP, list);
        }
        if (!this.orderedChunks.isEmpty()) {
            NBTTagList list = new NBTTagList();
            this.orderedChunks.forEach(pos -> {
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setInteger(TAG_CHUNK_X, pos.x);
                nbt1.setInteger(TAG_CHUNK_Z, pos.z);
                list.appendTag(nbt1);
            });
            nbt.setTag(TAG_ORDERED_CHUNKS, list);
        }
        if (this.destroyedList != null && !this.destroyedList.isEmpty()) {
            NBTTagList list = new NBTTagList();
            this.destroyedList.forEach(pos -> {
                NBTTagCompound nbt1 = new NBTTagCompound();
                nbt1.setInteger(TAG_POS_X, pos.getX());
                nbt1.setInteger(TAG_POS_Y, pos.getY());
                nbt1.setInteger(TAG_POS_Z, pos.getZ());
                list.appendTag(nbt1);
            });
            nbt.setTag(TAG_DESTROYED_LIST, list);
        }
    }

    private class RayTracerTask extends RecursiveAction {
        private static final int THRESHOLD = 512; // Number of rays to process in a single batch
        private static final double RAY_DIRECTION_EPSILON = 1e-6;
        private static final double PROCESSING_EPSILON = 1e-9;
        private static final float MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC = 0.01f;
        private final int start, end;
        private Map<ChunkPos, Map<Integer, Double>> localDamageMap;

        RayTracerTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected void compute() {
            if (directions == null) {
                directions = directionsFuture.join();
            }

            if (end - start <= THRESHOLD) {
                this.localDamageMap = (algorithm == 2) ? new HashMap<>() : null;

                for (int i = start; i < end; i++) {
                    if (Thread.currentThread().isInterrupted()) return;
                    trace(i);
                }

                if (localDamageMap != null && !localDamageMap.isEmpty()) {
                    for (Map.Entry<ChunkPos, Map<Integer, Double>> chunkEntry : localDamageMap.entrySet()) {
                        ChunkPos cp = chunkEntry.getKey();
                        ConcurrentMap<Integer, DoubleAdder> globalChunkDamage = damageMap.computeIfAbsent(cp, k -> new ConcurrentHashMap<>());

                        for (Map.Entry<Integer, Double> damageEntry : chunkEntry.getValue().entrySet()) {
                            globalChunkDamage.computeIfAbsent(damageEntry.getKey(), k -> new DoubleAdder()).add(damageEntry.getValue());
                        }
                    }
                }
            } else {
                int mid = start + (end - start) / 2;
                invokeAll(new RayTracerTask(start, mid), new RayTracerTask(mid, end));
            }
        }

        void trace(int dirIndex) {
            Vec3d dir = directions.get(dirIndex);
            float energy = strength * INITIAL_ENERGY_FACTOR;
            double px = explosionX;
            double py = explosionY;
            double pz = explosionZ;
            int x = originX;
            int y = originY;
            int z = originZ;
            double currentRayPosition = 0.0;

            double dirX = dir.x;
            double dirY = dir.y;
            double dirZ = dir.z;

            double absDirX = Math.abs(dirX);
            int stepX = (absDirX < RAY_DIRECTION_EPSILON) ? 0 : (dirX > 0 ? 1 : -1);
            double tDeltaX = (stepX == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirX;
            double tMaxX = (stepX == 0) ? Double.POSITIVE_INFINITY : ((stepX > 0 ? (x + 1 - px) : (px - x)) * tDeltaX);

            double absDirY = Math.abs(dirY);
            int stepY = (absDirY < RAY_DIRECTION_EPSILON) ? 0 : (dirY > 0 ? 1 : -1);
            double tDeltaY = (stepY == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirY;
            double tMaxY = (stepY == 0) ? Double.POSITIVE_INFINITY : ((stepY > 0 ? (y + 1 - py) : (py - y)) * tDeltaY);

            double absDirZ = Math.abs(dirZ);
            int stepZ = (absDirZ < RAY_DIRECTION_EPSILON) ? 0 : (dirZ > 0 ? 1 : -1);
            double tDeltaZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : 1.0 / absDirZ;
            double tMaxZ = (stepZ == 0) ? Double.POSITIVE_INFINITY : ((stepZ > 0 ? (z + 1 - pz) : (pz - z)) * tDeltaZ);

            int lastCX = Integer.MIN_VALUE, lastCZ = Integer.MIN_VALUE, lastSubY = Integer.MIN_VALUE;
            SubChunkKey currentSubChunkKey = null;

            try {
                if (energy <= 0) return;

                while (energy > 0) {
                    if (y < 0 || y >= WORLD_HEIGHT || Thread.currentThread().isInterrupted()) break;
                    if (currentRayPosition >= radius - PROCESSING_EPSILON) break;
                    Block block;
                    ChunkPos chunkPos;
                    int cx = x >> 4;
                    int cz = z >> 4;

                    if (unsafeAccess && world instanceof WorldServer server && server.getChunkProvider().chunkExists(cx, cz)) {
                        block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                        chunkPos = new ChunkPos(cx, cz);
                    } else {
                        int subY = y >> 4;
                        if (cx != lastCX || cz != lastCZ || subY != lastSubY) {
                            currentSubChunkKey = new SubChunkKey(cx, cz, subY);
                            lastCX = cx;
                            lastCZ = cz;
                            lastSubY = subY;
                        }
                        SubChunkSnapshot snap = snapshots.get(currentSubChunkKey);
                        if (snap == null) {
                            final boolean[] amFirst = {false};
                            ConcurrentLinkedQueue<RayTracerTask> waiters = waitingRoom.computeIfAbsent(currentSubChunkKey, k -> {
                                amFirst[0] = true;
                                return new ConcurrentLinkedQueue<>();
                            });
                            if (amFirst[0]) reactiveSnapQueue.add(currentSubChunkKey);

                            waiters.add(new RayTracerTask(dirIndex, dirIndex + 1));
                            return;
                        }

                        if (snap == SubChunkSnapshot.EMPTY) {
                            block = Blocks.AIR;
                        } else {
                            block = snap.getBlock(x & 0xF, y & 0xF, z & 0xF);
                        }
                        chunkPos = currentSubChunkKey.getPos();
                    }

                    double t_exit_voxel = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
                    double segmentLenInVoxel = t_exit_voxel - currentRayPosition;
                    double segmentLenForProcessing;
                    boolean stopAfterThisSegment = false;

                    if (currentRayPosition + segmentLenInVoxel > radius - PROCESSING_EPSILON) {
                        segmentLenForProcessing = Math.max(0.0, radius - currentRayPosition);
                        stopAfterThisSegment = true;
                    } else {
                        segmentLenForProcessing = segmentLenInVoxel;
                    }

                    if (block != Blocks.AIR && segmentLenForProcessing > PROCESSING_EPSILON) {
                        float resistance = getNukeResistance(block);
                        if (resistance >= NUKE_RESISTANCE_CUTOFF) {
                            energy = 0;
                        } else {
                            double energyLossFactor = getEnergyLossFactor(resistance, currentRayPosition);
                            float damageDealt = (float) (energyLossFactor * segmentLenForProcessing);
                            energy -= damageDealt;

                            if (damageDealt > 0) {
                                int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((x & 0xF) << 4) | (z & 0xF);
                                if (algorithm == 2) {
                                    localDamageMap.computeIfAbsent(chunkPos, k -> new HashMap<>()).merge(bitIndex, (double) damageDealt, Double::sum);
                                } else if (energy > 0) {
                                    destructionMap.computeIfAbsent(chunkPos, posKey -> new ConcurrentBitSet(BITSET_SIZE)).set(bitIndex);
                                }
                            }
                        }
                    }

                    currentRayPosition = t_exit_voxel;

                    if (energy <= 0 || stopAfterThisSegment) break;
                    if (tMaxX < tMaxY) {
                        if (tMaxX < tMaxZ) {
                            x += stepX;
                            tMaxX += tDeltaX;
                        } else {
                            z += stepZ;
                            tMaxZ += tDeltaZ;
                        }
                    } else {
                        if (tMaxY < tMaxZ) {
                            y += stepY;
                            tMaxY += tDeltaY;
                        } else {
                            z += stepZ;
                            tMaxZ += tDeltaZ;
                        }
                    }
                }
                if (energy > 0) ExplosionNukeRayParallelized.this.isContained = false;

            } catch (Exception e) {
                MainRegistry.logger.error("A ray in batch {}-{} finished exceptionally: ", start, end, e);
            }
        }

        private double getEnergyLossFactor(float resistance, double currentRayPosition) {
            double effectiveDist = Math.max(currentRayPosition, MIN_EFFECTIVE_DIST_FOR_ENERGY_CALC);
            double distFrac = effectiveDist / radius;
            if (resistance >= NUKE_RESISTANCE_CUTOFF) return resistance;
            if (resistance <= 0) return 0.0;
            if (resistance > LUT_MAX_RESISTANCE) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
            int rBin = (int) (resistance * (LUT_RESISTANCE_BINS - 1) / LUT_MAX_RESISTANCE);
            if (rBin == 0 && resistance > 0) return Math.pow(resistance + 1.0, 3.0 * distFrac) - 1.0;
            int dBin = (int) (distFrac * (LUT_DISTANCE_BINS - 1));
            return ENERGY_LOSS_LUT[rBin][dBin];
        }
    }
}