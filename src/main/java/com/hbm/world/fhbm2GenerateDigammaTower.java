package com.hbm.world;

import com.hbm.lib.RefStrings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class fhbm2GenerateDigammaTower implements IWorldGenerator {

    private static final int STRUCTURE_SIZE = 18;
    private static final String STRUCTURE_NAME = "digamma_tower";
    private static final double BASE_SPAWN_CHANCE = 5;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, net.minecraft.world.gen.IChunkGenerator chunkGenerator, net.minecraft.world.chunk.IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;

        double spawnChance = BASE_SPAWN_CHANCE;

        if (world.getWorldType() == WorldType.FLAT) {
            spawnChance = spawnChance / 50;
        }

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHeight(x, z);
        BlockPos origin = new BlockPos(x, y, z);

        if (isFlatAndClear(world, origin, STRUCTURE_SIZE)) {
            if (random.nextDouble() * 100 < spawnChance) {
                this.spawnStructure(world, origin);
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
                .getTemplate(world.getMinecraftServer(), new ResourceLocation(RefStrings.MODID, STRUCTURE_NAME));

        if (template != null) {
            PlacementSettings settings = new PlacementSettings();
            BlockPos structureSize = template.getSize();
            BlockPos adjustedPos = pos.add((STRUCTURE_SIZE - structureSize.getX()) / 2, 0, (STRUCTURE_SIZE - structureSize.getZ()) / 2);

            template.addBlocksToWorld(world, adjustedPos, settings);
            System.out.println("Generated structure: " + STRUCTURE_NAME + " at " + adjustedPos);
        } else {
            System.err.println("Could not find structure: " + STRUCTURE_NAME);
        }
    }
}
