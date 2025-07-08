package com.hbm.blocks.machine;

import com.hbm.tileentity.machine.TileEntityFoundryMold;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class FoundryMold extends FoundryCastingBase {

	public FoundryMold(String s) {
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryMold();
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		addBox(pos, entityBox, collidingBoxes, 0.0,   0.0,    0.0,   1.0,   0.125, 1.0);
		addBox(pos, entityBox, collidingBoxes, 0.0,   0.125,  0.0,   1.0,   0.5,   0.125);
		addBox(pos, entityBox, collidingBoxes, 0.0,   0.125,  0.875, 1.0,   0.5,   1.0);
		addBox(pos, entityBox, collidingBoxes, 0.0,   0.125,  0.125, 0.125, 0.5,   0.875);
		addBox(pos, entityBox, collidingBoxes, 0.875, 0.125,  0.125, 1.0,   0.5,   0.875);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}

	@Override
	public boolean isSideSolid(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return side != EnumFacing.UP;
	}

	@Override
	public double getPH(){ //particle height
		return 0.25;
	}
}
