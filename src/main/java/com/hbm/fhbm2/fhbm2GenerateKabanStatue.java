package com.hbm.fhbm2;

import com.hbm.lib.RefStrings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class fhbm2GenerateKabanStatue implements IWorldGenerator {

    private static final int STRUCTURE_SIZE = 3;
    private static final String KABAN_STATUE = "kaban_statue";
    private static final int BASE_SPAWN_CHANCE = 1;

    private static final Set<String> ALLOWED_BIOMES = new HashSet<>();
    static {
        ALLOWED_BIOMES.add("minecraft:desert");
        ALLOWED_BIOMES.add("minecraft:jungle");
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, net.minecraft.world.gen.IChunkGenerator chunkGenerator, net.minecraft.world.chunk.IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;
        if (world.getWorldType() == WorldType.FLAT) return;

        int spawnChance = BASE_SPAWN_CHANCE;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHeight(x, z);
        BlockPos origin = new BlockPos(x, y, z);

        Biome biome = world.getBiome(origin);
        if (biome.getRegistryName() != null && ALLOWED_BIOMES.contains(biome.getRegistryName().toString())) {

            if (isFlatAndClear(world, origin, STRUCTURE_SIZE)) {
                if (random.nextInt(100) < spawnChance) {
                    spawnStructure(world, origin);
                }
            }
        }
    }

    private boolean isFlatAndClear(World world, BlockPos origin, int size) {
        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                BlockPos checkPos = origin.add(dx, 0, dz);
                IBlockState state = world.getBlockState(checkPos);

                if (state.getMaterial().isSolid() && !state.getBlock().isAir(state, world, checkPos)) {
                    return false;
                }

                if (!world.getBlockState(checkPos.down()).isOpaqueCube()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void spawnStructure(World world, BlockPos pos) {
        Template template = world.getSaveHandler().getStructureTemplateManager()
                .getTemplate(world.getMinecraftServer(), new ResourceLocation(RefStrings.MODID, KABAN_STATUE));

        if (template != null) {
            PlacementSettings settings = new PlacementSettings();
            BlockPos structureSize = template.getSize();
            BlockPos adjustedPos = pos.add((STRUCTURE_SIZE - structureSize.getX()) / 2, 0, (STRUCTURE_SIZE - structureSize.getZ()) / 2);

            template.addBlocksToWorld(world, adjustedPos, settings);
            System.out.println("Generated structure: " + KABAN_STATUE + " at " + adjustedPos);
        } else {
            System.err.println("Could not find structure: " + KABAN_STATUE);
        }
    }
}