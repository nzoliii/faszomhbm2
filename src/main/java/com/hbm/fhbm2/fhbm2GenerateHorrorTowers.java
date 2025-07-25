package com.hbm.fhbm2;

import com.hbm.lib.RefStrings;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.IWorldGenerator;

public class fhbm2GenerateHorrorTowers implements IWorldGenerator {

    // back in the day when there were 2 versions of the horror towers
    // now it is just one tower, and im lazy to rename fhbm2GenerateHorrorTowers.java
    // also this is a clusterfuck because it ignores HBM's laws and configs
    // same with every fhbm2 worldgen code, all of them are clusterfucks.
    // fixme

    private static final int STRUCTURE_SIZE = 18;
    private static final String DIGAMMA_TOWER = "fhbm2_digamma_tower";
    private static final int BASE_SPAWN_CHANCE = 1;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, net.minecraft.world.gen.IChunkGenerator chunkGenerator, net.minecraft.world.chunk.IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return;
        if (world.getWorldType() == WorldType.FLAT) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);
        int y = world.getHeight(x, z);
        BlockPos origin = new BlockPos(x, y, z);

        if (isFlatAndClear(world, origin, STRUCTURE_SIZE)) {
            if (random.nextInt(100) < BASE_SPAWN_CHANCE) {
                spawnStructure(world, origin);
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
                .getTemplate(world.getMinecraftServer(), new ResourceLocation(RefStrings.MODID, DIGAMMA_TOWER));

        if (template != null) {
            PlacementSettings settings = new PlacementSettings();
            BlockPos structureSize = template.getSize();
            BlockPos adjustedPos = pos.add((STRUCTURE_SIZE - structureSize.getX()) / 2, 0, (STRUCTURE_SIZE - structureSize.getZ()) / 2);

            template.addBlocksToWorld(world, adjustedPos, settings);
            System.out.println("[fhbm2GenerateHorrorTowers] Generated structure: " + DIGAMMA_TOWER + " at " + adjustedPos);
        } else {
            System.err.println("[fhbm2GenerateHorrorTowers] Could not find structure: " + DIGAMMA_TOWER);
        }
    }
}