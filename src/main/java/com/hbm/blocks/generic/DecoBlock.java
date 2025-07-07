package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.deco.TileEntityDecoBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class DecoBlock extends BlockContainer {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private static final float f = 0.0625F;
    public static final AxisAlignedBB WALL_WEST_BOX = new AxisAlignedBB(14 * f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_EAST_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 2 * f, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_NORTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 14 * f, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB WALL_SOUTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2 * f);
    public static final AxisAlignedBB STEEL_ROOF_BOX = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1 * f, 1.0F);
    public static final AxisAlignedBB STEEL_BEAM_BOX = new AxisAlignedBB(7 * f, 0.0F, 7 * f, 9 * f, 1.0F, 9 * f);
    public static final AxisAlignedBB SCAFFOLD_EASTWEST_BOX = new AxisAlignedBB(2 * f, 0.0F, 0.0F, 14 * f, 1.0F, 1.0F);
    public static final AxisAlignedBB SCAFFOLD_NORTHSOUTH_BOX = new AxisAlignedBB(0.0F, 0.0F, 2 * f, 1.0F, 1.0F, 14 * f);

    public DecoBlock(Material materialIn, String s) {
        super(materialIn);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setCreativeTab(MainRegistry.blockTab);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public @NotNull Block setSoundType(@NotNull SoundType sound) {
        return super.setSoundType(sound);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World world, int meta) {
        if (this == ModBlocks.steel_scaffold || this == ModBlocks.steel_beam)
            return null;
        return new TileEntityDecoBlock();
    }

    @Override
    public boolean hasTileEntity(@NotNull IBlockState state) {
        return this != ModBlocks.steel_beam;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        if (this == ModBlocks.steel_beam || this == ModBlocks.steel_scaffold)
            return EnumBlockRenderType.MODEL;
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing()));
    }

    @Override
    public @NotNull Item getItemDropped(@NotNull IBlockState state, @NotNull Random rand, int fortune) {
        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        EnumFacing te = state.getValue(FACING);
        if (this == ModBlocks.steel_wall) {
            return switch (te) {
                case WEST -> WALL_WEST_BOX;
                case NORTH -> WALL_NORTH_BOX;
                case EAST -> WALL_EAST_BOX;
                case SOUTH -> WALL_SOUTH_BOX;
                default -> FULL_BLOCK_AABB;
            };
        } else if (this == ModBlocks.steel_roof) {
            return STEEL_ROOF_BOX;
        } else if (this == ModBlocks.steel_beam) {
            return STEEL_BEAM_BOX;
        } else if (this == ModBlocks.steel_scaffold) {
            return switch (te) {
                case WEST, EAST -> SCAFFOLD_EASTWEST_BOX;
                case NORTH, SOUTH -> SCAFFOLD_NORTHSOUTH_BOX;
                default -> FULL_BLOCK_AABB;
            };
        }
        return FULL_BLOCK_AABB;
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta);

        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, facing);
    }


    @Override
    public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

}
