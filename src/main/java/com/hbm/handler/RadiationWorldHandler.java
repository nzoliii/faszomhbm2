package com.hbm.handler;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.RadiationSystemNT.RadPocket;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.RadiationSaveStructure;
import com.hbm.saveddata.RadiationSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;

import java.util.Collection;
import java.util.Map.Entry;

public class RadiationWorldHandler {

    public static void handleWorldDestruction(World world) {
        if (!(world instanceof WorldServer)) return;
        if (!RadiationConfig.worldRadEffects || !GeneralConfig.enableRads) return;
        if (GeneralConfig.advancedRadiation) {
            handleAdvancedDestruction(world);
        } else {
            handleLegacyDestruction(world);
        }
    }

    private static void handleAdvancedDestruction(World world) {
        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Starting advanced world destruction processing");
        }

        Collection<RadPocket> activePockets = RadiationSystemNT.getActiveCollection(world);
        if (activePockets.isEmpty()) {
            return;
        }

        RadPocket[] pockets = activePockets.toArray(new RadPocket[0]);
        RadPocket p = pockets[world.rand.nextInt(pockets.length)];

        float threshold = 5.0F;

        if (p.radiation.get() < threshold) {
            return;
        }

        BlockPos startPos = p.parent.subChunkPos;
        RadPocket[] pocketsByBlock = p.parent.pocketsByBlock;

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                for (int k = 0; k < 16; k++) {
                    if (world.rand.nextInt(3) != 0) continue;
                    if (pocketsByBlock != null && pocketsByBlock[i * 256 + j * 16 + k] != p) continue;

                    BlockPos pos = startPos.add(i, j, k);
                    if (world.isAirBlock(pos)) continue;

                    IBlockState state = world.getBlockState(pos);
                    decayBlock(world, pos, state, false);
                }
            }
        }

        if (GeneralConfig.enableDebugMode) {
            MainRegistry.logger.info("[Debug] Finished advanced world destruction processing");
        }
    }

    private static void handleLegacyDestruction(World world) {
        WorldServer serv = (WorldServer) world;
        RadiationSavedData data = RadiationSavedData.getData(serv);
        ChunkProviderServer provider = serv.getChunkProvider();
        Object[] entries = data.contamination.entrySet().toArray();

        if (entries.length == 0) return;

        @SuppressWarnings("unchecked") Entry<ChunkPos, RadiationSaveStructure> randEnt =
                (Entry<ChunkPos, RadiationSaveStructure>) entries[world.rand.nextInt(entries.length)];
        ChunkPos coords = randEnt.getKey();
        float threshold = 5.0F;

        if (randEnt.getValue().radiation < threshold) return;
        if (!provider.chunkExists(coords.x, coords.z)) return;

        for (int a = 0; a < 16; a++) {
            for (int b = 0; b < 16; b++) {
                if (world.rand.nextInt(3) != 0) continue;

                int x = coords.getXStart() + a;
                int z = coords.getZStart() + b;
                int y = world.getHeight(x, z) - world.rand.nextInt(2);
                BlockPos pos = new BlockPos(x, y, z);

                if (world.isAirBlock(pos)) continue;

                IBlockState state = world.getBlockState(pos);
                decayBlock(world, pos, state, true);
            }
        }
    }

    private static void decayBlock(World world, BlockPos pos, IBlockState state, boolean isLegacy) {
        Block block = state.getBlock();
        if (block.getRegistryName() == null) return;
        String registryName = block.getRegistryName().toString();

        if ("hbm:waste_leaves".equals(registryName)) {
            if (world.rand.nextInt(8) == 0) {
                world.setBlockToAir(pos);
            }
            return;
        }

        IBlockState newState = switch (registryName) {
            case "minecraft:grass" -> ModBlocks.waste_earth.getDefaultState();
            case "minecraft:dirt", "minecraft:farmland" -> ModBlocks.waste_dirt.getDefaultState();
            case "minecraft:sandstone" -> ModBlocks.waste_sandstone.getDefaultState();
            case "minecraft:red_sandstone" -> ModBlocks.waste_red_sandstone.getDefaultState();
            case "minecraft:hardened_clay", "minecraft:stained_hardened_clay" -> ModBlocks.waste_terracotta.getDefaultState();
            case "minecraft:gravel" -> ModBlocks.waste_gravel.getDefaultState();
            case "minecraft:mycelium" -> ModBlocks.waste_mycelium.getDefaultState();
            case "minecraft:snow_layer" -> ModBlocks.waste_snow.getDefaultState();
            case "minecraft:snow" -> ModBlocks.waste_snow_block.getDefaultState();
            case "minecraft:ice" -> ModBlocks.waste_ice.getDefaultState();
            case "minecraft:sand" -> {
                BlockSand.EnumType meta = state.getValue(BlockSand.VARIANT);
                if (isLegacy && world.rand.nextInt(60) == 0) {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_trinitite.getDefaultState() :
                            ModBlocks.waste_trinitite_red.getDefaultState();
                } else {
                    yield meta == BlockSand.EnumType.SAND ? ModBlocks.waste_sand.getDefaultState() : ModBlocks.waste_sand_red.getDefaultState();
                }
            }
            default -> {
                if (block instanceof BlockLeaves) {
                    yield ModBlocks.waste_leaves.getDefaultState();
                } else if (block instanceof BlockBush) {
                    yield ModBlocks.waste_grass_tall.getDefaultState();
                }
                yield null;
            }
        };

        if (newState != null) world.setBlockState(pos, newState);
    }
}