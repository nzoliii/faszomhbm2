package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDeadPlant;
import com.hbm.blocks.generic.BlockPlantEnumMeta;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import static com.hbm.blocks.PlantEnums.EnumDeadPlantType;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_1;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;
import static com.hbm.blocks.generic.BlockMeta.META;

public class OilSpot {

    public static void generateOilSpot(World world, int x, int z, int width, int count, boolean addWillows) {

        for(int i = 0; i < count; i++) {
            int rX = x + (int)(world.rand.nextGaussian() * width);
            int rZ = z + (int)(world.rand.nextGaussian() * width);
            int rY = world.getHeight(rX, rZ);

            BlockPos pos = new BlockPos(rX, rY - 1, rZ);
            BlockPos plantPos = new BlockPos(rX, rY, rZ);

            for(int y = rY; y > rY - 4; y--) {

                Block below = world.getBlockState(pos).getBlock();
                Block ground = world.getBlockState(plantPos).getBlock();
                IBlockState type = world.getBlockState(new BlockPos(rX, y, rZ));

                if(type.getBlock() instanceof BlockPlantEnumMeta) {
                    int meta = type.getValue(META);
                    if (ground == ModBlocks.plant_flower && (meta == MUSTARD_WILLOW_0.ordinal() || meta == MUSTARD_WILLOW_1.ordinal()))
                        continue;
                    if (ground == ModBlocks.plant_tall && (meta == MUSTARD_WILLOW_2_LOWER.ordinal() || meta == MUSTARD_WILLOW_3_LOWER.ordinal() || meta == MUSTARD_WILLOW_4_LOWER.ordinal()))
                        continue;
                }

                if(below.isNormalCube(below.getDefaultState(), world, pos) && !(ground instanceof BlockDeadPlant)) {
                    if(ground instanceof BlockTallGrass) {
                        if(world.rand.nextInt(10) == 0) {
                            Block block = world.getBlockState(new BlockPos(rX, rY + 1, rZ)).getBlock();
                            if (block.getMetaFromState(block.getBlockState().getBaseState()) == 2) {
                                world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FERN.ordinal()));
                            } else {
                                world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GRASS.ordinal()));
                            }
                        } else {
                            world.setBlockState(plantPos, Blocks.AIR.getDefaultState());
                        }
                    } else if(ground instanceof BlockFlower) {
                        world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.FLOWER.ordinal()));
                    } else if(ground instanceof BlockDoublePlant) {
                        world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.BIG_FLOWER.ordinal()));
                    } else if(ground instanceof BlockBush) {
                        world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()));
                    } else if(ground instanceof IPlantable) {
                        world.setBlockState(plantPos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()));
                    }
                }

                if(below == Blocks.GRASS || below == Blocks.DIRT) {
                    world.setBlockState(pos, world.rand.nextInt(10) == 0 ? ModBlocks.dirt_oily.getDefaultState() : ModBlocks.dirt_dead.getDefaultState());

                    if(addWillows && world.rand.nextInt(50) == 0) {
                        BlockPos targetPos = new BlockPos(rX, y + 1, rZ);
                        if(ModBlocks.plant_flower.canPlaceBlockAt(world, targetPos )) {
                            world.setBlockState(targetPos, ModBlocks.plant_flower.getDefaultState().withProperty(META, MUSTARD_WILLOW_0.ordinal()), 3);
                        }
                    }

                    break;

                } else if(below == Blocks.SAND || below == ModBlocks.ore_oil_sand) {

                    IBlockState blockState = world.getBlockState(pos);
                    if(blockState.getBlock() == Blocks.SAND && blockState.getValue(BlockSand.VARIANT) == BlockSand.EnumType.RED_SAND)
                        world.setBlockState(pos, ModBlocks.sand_dirty_red.getDefaultState());
                    else
                        world.setBlockState(pos, ModBlocks.sand_dirty.getDefaultState());
                    break;

                } else if(below == Blocks.STONE) {
                    world.setBlockState(pos, ModBlocks.stone_cracked.getDefaultState());
                    break;

                } else if(below.getDefaultState().getMaterial() == Material.LEAVES) {
                    world.setBlockToAir(pos);
                    break;
                }
            }
        }
    }
}