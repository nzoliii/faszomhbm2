package com.hbm.entity.effect;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.config.WorldConfig;
import com.hbm.interfaces.IConstantRenderer;
import com.hbm.saveddata.AuxSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class EntityFalloutRain extends EntityFallout implements IConstantRenderer {

	private static final int WORLD_HEIGHT = 256;
	private static final int BITSET_SIZE = 16 * WORLD_HEIGHT * 16;
	private static final int SUBCHUNK_PER_CHUNK = WORLD_HEIGHT >> 4;

	public boolean doFallout = false;
	public boolean doFlood = false;
	public boolean doDrop = false;
	public int waterLevel;
	public boolean spawnFire = false;
	public int falloutBallRadius = 0;
	private int fallingRadius;
	private boolean firstTick = true;
	private final List<Long> chunksToProcess = new ArrayList<>();
	private final List<Long> outerChunksToProcess = new ArrayList<>();
	private int falloutTickNumber = 0;
	private boolean stompingDone = false;
	private boolean drainFinished = false;
	private final Map<ChunkPos, BitSet> drainMap = new HashMap<>();
	private final List<ChunkPos> orderedDrainChunks = new ArrayList<>();
	private List<BlockPos> drainedList = new ArrayList<>();

	public EntityFalloutRain(World world) {
		super(world);
		this.setSize(4, 20);
		this.waterLevel = getInt(CompatibilityConfig.fillCraterWithWater.get(world.provider.getDimension()));
		if (this.waterLevel == 0) {
			this.waterLevel = world.getSeaLevel();
		} else if (this.waterLevel < 0 && this.waterLevel > -world.getSeaLevel()) {
			this.waterLevel = world.getSeaLevel() - this.waterLevel;
		}
		this.spawnFire = BombConfig.spawnFire;
		this.drainFinished = this.doFlood;
	}

	private static int getInt(Object e) {
		return WorldConfig.convertToInt(e);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.posX, this.posY, this.posZ, this.posX, this.posY, this.posZ);
	}

	@Override
	public boolean isInRangeToRender3d(double x, double y, double z) {
		return true;
	}

	@Override
	public boolean isInRangeToRenderDist(double distance) {
		return true;
	}

	public void setScale(int i, int craterRadius) {
		dataManager.set(SCALE, i);
		calculateS(i);
		fallingRadius = craterRadius;
		doDrop = fallingRadius > 20;
	}

	@Override
	protected double getFallingRadius() {
		return fallingRadius;
	}

	@Override
	protected boolean getSpawnFire() {
		return spawnFire;
	}

	@Override
	public void onUpdate() {
		if (!world.isRemote) {
			if (!CompatibilityConfig.isWarDim(world)) {
				this.setDead();
			} else if (firstTick) {
				if (chunksToProcess.isEmpty() && outerChunksToProcess.isEmpty()) gatherChunks();
				firstTick = false;
			}
			if (falloutTickNumber >= BombConfig.fChunkSpeed) {
				if (!this.isDead) {
					long start = System.currentTimeMillis();
					final long timeBudget = BombConfig.falloutMS;
					long deadline = start + timeBudget;
					while (System.currentTimeMillis() < deadline && !stompingDone) {
						stompAround();
					}
					while (System.currentTimeMillis() < deadline && stompingDone && !drainFinished) {
						drainTick();
					}
					if (stompingDone && drainFinished) {
						secondPassDrain();
						setDead();
					}
				}
				falloutTickNumber = 0;
			}
			falloutTickNumber++;

			if (this.isDead) {
				if (falloutBallRadius > 0) {
					EntityFalloutUnderGround falloutBall = new EntityFalloutUnderGround(this.world);
					falloutBall.posX = this.posX;
					falloutBall.posY = this.posY;
					falloutBall.posZ = this.posZ;
					falloutBall.setScale(falloutBallRadius);
					this.world.spawnEntity(falloutBall);
				}
				unloadAllChunks();
				this.done = true;
				if (RadiationConfig.rain > 0 && doFlood) {
					if ((doFallout && getScale() > 100) || (doFlood && getScale() > 50)) {
						world.getWorldInfo().setRaining(true);
						world.getWorldInfo().setRainTime(RadiationConfig.rain);
					}
					if ((doFallout && getScale() > 150) || (doFlood && getScale() > 100)) {
						world.getWorldInfo().setThundering(true);
						world.getWorldInfo().setThunderTime(RadiationConfig.rain);
						AuxSavedData.setThunder(world, RadiationConfig.rain);
					}
				}
			}
		}
	}

	private void gatherChunks() {
		Set<Long> chunks = new LinkedHashSet<>();
		Set<Long> outerChunks = new LinkedHashSet<>();
		int outerRange = doFallout ? getScale() : fallingRadius;
		int adjustedMaxAngle = 20 * outerRange / 32;
		for (int angle = 0; angle <= adjustedMaxAngle; angle++) {
			Vec3d vector = new Vec3d(outerRange, 0, 0);
			vector = vector.rotateYaw((float) (angle * Math.PI / 180.0 / (adjustedMaxAngle / 360.0)));
			outerChunks.add(ChunkPos.asLong((int) (posX + vector.x) >> 4, (int) (posZ + vector.z) >> 4));
		}
		for (int distance = 0; distance <= outerRange; distance += 8) for (int angle = 0; angle <= adjustedMaxAngle; angle++) {
			Vec3d vector = new Vec3d(distance, 0, 0);
			vector = vector.rotateYaw((float) (angle * Math.PI / 180.0 / (adjustedMaxAngle / 360.0)));
			long chunkCoord = ChunkPos.asLong((int) (posX + vector.x) >> 4, (int) (posZ + vector.z) >> 4);
			if (!outerChunks.contains(chunkCoord)) chunks.add(chunkCoord);
		}

		chunksToProcess.addAll(chunks);
		outerChunksToProcess.addAll(outerChunks);
		Collections.reverse(chunksToProcess);
		Collections.reverse(outerChunksToProcess);
	}

	private void stompAround() {
		if (!chunksToProcess.isEmpty()) {
			long chunkPos = chunksToProcess.remove(chunksToProcess.size() - 1);
			int chunkPosX = (int) (chunkPos & Integer.MAX_VALUE);
			int chunkPosZ = (int) (chunkPos >> 32 & Integer.MAX_VALUE);
			for (int x = chunkPosX << 4; x < (chunkPosX << 4) + 16; x++) {
				for (int z = chunkPosZ << 4; z < (chunkPosZ << 4) + 16; z++) {
					double dist = Math.hypot(x - posX, z - posZ);
					stomp(new MutableBlockPos(x, 0, z), dist);
				}
			}

		} else if (!outerChunksToProcess.isEmpty()) {
			long chunkPos = outerChunksToProcess.remove(outerChunksToProcess.size() - 1);
			int chunkPosX = (int) (chunkPos & Integer.MAX_VALUE);
			int chunkPosZ = (int) (chunkPos >> 32 & Integer.MAX_VALUE);
			for (int x = chunkPosX << 4; x < (chunkPosX << 4) + 16; x++) {
				for (int z = chunkPosZ << 4; z < (chunkPosZ << 4) + 16; z++) {
					double dist = Math.hypot(x - posX, z - posZ);
					if (dist <= getScale()) {
						stomp(new MutableBlockPos(x, 0, z), dist);
					}
				}
			}

		} else {
			stompingDone = true;
		}
	}

	private void stomp(MutableBlockPos pos, double dist) {
		if (dist > s0) {
			if (world.rand.nextFloat() > 0.05F + (5F * (s0 / dist) - 4F)) {
				return;
			}
		}
		int[] gapData;
		if (doFallout) {
			gapData = doFallout(pos, dist);
		} else {
			gapData = doNoFallout(pos, dist);
		}
		if (dist < fallingRadius) {
			if (doDrop && gapData[0] == 1) {
				letFall(world, pos, gapData[1], gapData[2]);
			}
			if (doFlood) {
				flood(pos);
			} else {
				collectDrain(pos);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private int[] doFallout(MutableBlockPos pos, double dist) {
		int stoneDepth = 0;
		int maxStoneDepth = 0;
		if (dist > s1) maxStoneDepth = 0;
		else if (dist > s2) maxStoneDepth = 1;
		else if (dist > s3) maxStoneDepth = 2;
		else if (dist > s4) maxStoneDepth = 3;
		else if (dist > s5) maxStoneDepth = 4;
		else if (dist > s6) maxStoneDepth = 5;
		else maxStoneDepth = 6;
		boolean lastReachedStone = false;
		boolean reachedStone = false;
		int contactHeight = 420;
		int lastGapHeight = 420;
		boolean gapFound = false;
		outer: for (int y = 255; y >= 0; y--) {
			pos.setY(y);
			IBlockState blockState = world.getBlockState(pos);
			Block block = blockState.getBlock();
			Material bmaterial = blockState.getMaterial();
			if (block != Blocks.AIR && contactHeight == 420) contactHeight = Math.min(y + 1, 255);
			lastReachedStone = reachedStone;
			reachedStone = bmaterial == Material.ROCK;
			if (reachedStone && bmaterial != Material.AIR) stoneDepth++;
			if (reachedStone && stoneDepth > maxStoneDepth) break outer;
			if (bmaterial == Material.AIR || bmaterial.isLiquid()) {
				if (y < contactHeight) {
					gapFound = true;
					lastGapHeight = y;
				}
				continue;
			}
			if (!processBlock(world, pos, dist, true, stoneDepth, maxStoneDepth, reachedStone, lastReachedStone, contactHeight)) {
				break;
			}
		}
		return new int[]{gapFound ? 1 : 0, lastGapHeight, contactHeight};
	}

	private int[] doNoFallout(MutableBlockPos pos, double dist) {
		int stoneDepth = 0;
		int maxStoneDepth = 6;
		boolean reachedStone = false;
		int contactHeight = 420;
		int lastGapHeight = 420;
		boolean gapFound = false;
		for (int y = 255; y >= 0; y--) {
			pos.setY(y);
			IBlockState b = world.getBlockState(pos);
			Block bblock = b.getBlock();
			Material bmaterial = b.getMaterial();
			if (bblock.isCollidable() && contactHeight == 420) contactHeight = Math.min(y + 1, 255);
			if (reachedStone && bmaterial != Material.AIR) {
				stoneDepth++;
			} else {
				reachedStone = b.getMaterial() == Material.ROCK;
			}
			if (reachedStone && stoneDepth > maxStoneDepth) {
				break;
			}
			if (bmaterial == Material.AIR || bmaterial.isLiquid()) {
				if (y < contactHeight) {
					gapFound = true;
					lastGapHeight = y;
				}
			}
		}
		return new int[]{gapFound ? 1 : 0, lastGapHeight, contactHeight};
	}

	private void letFall(World world, MutableBlockPos pos, int lastGapHeight, int contactHeight) {
		int fallChance = RadiationConfig.blocksFallCh;
		if (fallChance < 1) return;
		if (fallChance < 100) {
			int chance = world.rand.nextInt(100);
			if (chance < fallChance) return;
		}
		int bottomHeight = lastGapHeight;
		MutableBlockPos gapPos = new MutableBlockPos(pos.getX(), 0, pos.getZ());
		for (int i = lastGapHeight; i <= contactHeight; i++) {
			pos.setY(i);
			Block b = world.getBlockState(pos).getBlock();
			if (!b.isReplaceable(world, pos)) {
				float hardness = b.getExplosionResistance(null);
				if (hardness >= 0 && hardness < 50 && i != bottomHeight) {
					gapPos.setY(bottomHeight);
					world.setBlockState(gapPos, world.getBlockState(pos));
					world.setBlockToAir(pos);
				}
				bottomHeight++;
			}
		}
	}

	private void flood(MutableBlockPos pos) {
		if (CompatibilityConfig.doFillCraterWithWater && waterLevel > 1) {
			for (int y = waterLevel - 1; y > 1; y--) {
				pos.setY(y);
				if (world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == Blocks.FLOWING_WATER) {
					world.setBlockState(pos, Blocks.WATER.getDefaultState());
				}
			}
		}
	}

	private void collectDrain(MutableBlockPos pos) {
		for (int y = 255; y > 1; y--) {
			pos.setY(y);
			IBlockState state = world.getBlockState(pos);
			Block b = state.getBlock();
			if (b == Blocks.WATER || b == Blocks.FLOWING_WATER) {
				int bitIndex = ((WORLD_HEIGHT - 1 - y) << 8) | ((pos.getX() & 0xF) << 4) | (pos.getZ() & 0xF);
				ChunkPos cp = new ChunkPos(pos);
				drainMap.computeIfAbsent(cp, k -> new BitSet(BITSET_SIZE)).set(bitIndex);
			}
		}
	}

	private void drainTick() {
		final long deadline = System.nanoTime() + BombConfig.falloutMS * 1_000_000L;

		if (orderedDrainChunks.isEmpty() && !drainMap.isEmpty()) {
			orderedDrainChunks.addAll(drainMap.keySet());
			int originChunkX = (int) posX >> 4;
			int originChunkZ = (int) posZ >> 4;
			orderedDrainChunks.sort(Comparator.comparingInt(c -> Math.abs(originChunkX - c.x) + Math.abs(originChunkZ - c.z)));
		}

		Iterator<ChunkPos> it = orderedDrainChunks.iterator();
		while (it.hasNext() && System.nanoTime() < deadline) {
			ChunkPos cp = it.next();
			BitSet bs = drainMap.get(cp);
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

					Block block = storage.get(xLocal, yLocal, zLocal).getBlock();
					if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
						BlockPos drainPos = new BlockPos(xGlobal, yGlobal, zGlobal);
						if (world.getTileEntity(drainPos) != null) world.removeTileEntity(drainPos);
						storage.set(xLocal, yLocal, zLocal, Blocks.AIR.getDefaultState());
						chunkModified = true;
						this.drainedList.add(drainPos);
					}
					bs.clear(bit);
					bit = bs.nextSetBit(bit + 1);
				}
			}
			if (chunkModified) chunk.markDirty();
			if (bs.isEmpty()) {
				drainMap.remove(cp);
				it.remove();
			}
		}
		if (orderedDrainChunks.isEmpty() && drainMap.isEmpty()) {
			drainFinished = true;
		}
	}

	private void secondPassDrain() {
		if (drainedList == null || drainedList.isEmpty()) return;
		Set<ChunkPos> modifiedChunks = new HashSet<>();
		for (BlockPos pos : drainedList) {
			world.notifyNeighborsOfStateChange(pos, Blocks.AIR, true);
			world.checkLightFor(EnumSkyBlock.SKY, pos);
			world.checkLightFor(EnumSkyBlock.BLOCK, pos);
			modifiedChunks.add(new ChunkPos(pos));
		}
		for (ChunkPos cp : modifiedChunks) {
			world.markBlockRangeForRenderUpdate(cp.x << 4, 0, cp.z << 4, (cp.x << 4) | 15, WORLD_HEIGHT - 1, (cp.z << 4) | 15);
		}
		drainedList.clear();
		drainedList = null;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setScale(nbt.getInteger("scale"), nbt.getInteger("dropRadius"));
		falloutBallRadius = nbt.getInteger("fBall");
		if (nbt.hasKey("chunks"))
			chunksToProcess.addAll(readChunksFromIntArray(nbt.getIntArray("chunks")));
		if (nbt.hasKey("outerChunks"))
			outerChunksToProcess.addAll(readChunksFromIntArray(nbt.getIntArray("outerChunks")));
		doFallout = nbt.getBoolean("doFallout");
		doFlood = nbt.getBoolean("doFlood");
		stompingDone = nbt.getBoolean("stompingDone");
		drainFinished = nbt.getBoolean("drainFinished");
		if (stompingDone && !drainFinished) {
			if (nbt.hasKey("drainMap", Constants.NBT.TAG_LIST)) {
				NBTTagList list = nbt.getTagList("drainMap", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound tag = list.getCompoundTagAt(i);
					ChunkPos pos = new ChunkPos(tag.getInteger("cX"), tag.getInteger("cZ"));
					long[] bitsetData = ((NBTTagLongArray) tag.getTag("bitset")).data;
					this.drainMap.put(pos, BitSet.valueOf(bitsetData));
				}
			}
			if (nbt.hasKey("orderedDrainChunks", Constants.NBT.TAG_LIST)) {
				NBTTagList list = nbt.getTagList("orderedDrainChunks", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound nbt1 = list.getCompoundTagAt(i);
					this.orderedDrainChunks.add(new ChunkPos(nbt1.getInteger("cX"), nbt1.getInteger("cZ")));
				}
			}
			if (nbt.hasKey("drainedList", Constants.NBT.TAG_LIST)) {
				NBTTagList list = nbt.getTagList("drainedList", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < list.tagCount(); i++) {
					NBTTagCompound nbt1 = list.getCompoundTagAt(i);
					this.drainedList.add(new BlockPos(nbt1.getInteger("pX"), nbt1.getInteger("pY"), nbt1.getInteger("pZ")));
				}
			}
		}
	}

	private Collection<Long> readChunksFromIntArray(int[] data) {
		List<Long> coords = new ArrayList<>();
		boolean firstPart = true;
		int x = 0;
		for (int coord : data) {
			if (firstPart)
				x = coord;
			else
				coords.add(ChunkPos.asLong(x, coord));
			firstPart = !firstPart;
		}
		return coords;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("scale", getScale());
		nbt.setInteger("fBall", falloutBallRadius);
		nbt.setInteger("dropRadius", fallingRadius);
		nbt.setBoolean("doFallout", doFallout);
		nbt.setBoolean("doFlood", doFlood);
		nbt.setBoolean("stompingDone", stompingDone);
		nbt.setBoolean("drainFinished", drainFinished);

		nbt.setIntArray("chunks", writeChunksToIntArray(chunksToProcess));
		nbt.setIntArray("outerChunks", writeChunksToIntArray(outerChunksToProcess));

		if (stompingDone && !drainFinished) {
			if (!this.drainMap.isEmpty()) {
				NBTTagList list = new NBTTagList();
				for (Map.Entry<ChunkPos, BitSet> entry : this.drainMap.entrySet()) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setInteger("cX", entry.getKey().x);
					tag.setInteger("cZ", entry.getKey().z);
					tag.setTag("bitset", new NBTTagLongArray(entry.getValue().toLongArray()));
					list.appendTag(tag);
				}
				nbt.setTag("drainMap", list);
			}
			if (!this.orderedDrainChunks.isEmpty()) {
				NBTTagList list = new NBTTagList();
				this.orderedDrainChunks.forEach(pos -> {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setInteger("cX", pos.x);
					nbt1.setInteger("cZ", pos.z);
					list.appendTag(nbt1);
				});
				nbt.setTag("orderedDrainChunks", list);
			}
			if (this.drainedList != null && !this.drainedList.isEmpty()) {
				NBTTagList list = new NBTTagList();
				this.drainedList.forEach(pos -> {
					NBTTagCompound nbt1 = new NBTTagCompound();
					nbt1.setInteger("pX", pos.getX());
					nbt1.setInteger("pY", pos.getY());
					nbt1.setInteger("pZ", pos.getZ());
					list.appendTag(nbt1);
				});
				nbt.setTag("drainedList", list);
			}
		}
	}

	private int[] writeChunksToIntArray(List<Long> coords) {
		int[] data = new int[coords.size() * 2];
		for (int i = 0; i < coords.size(); i++) {
			data[i * 2] = (int) (coords.get(i) & Integer.MAX_VALUE);
			data[i * 2 + 1] = (int) (coords.get(i) >> 32 & Integer.MAX_VALUE);
		}
		return data;
	}
}