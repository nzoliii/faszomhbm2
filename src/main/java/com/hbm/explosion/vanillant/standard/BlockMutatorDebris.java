package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.inventory.RecipesCommon;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMutatorDebris implements IBlockMutator {

	protected RecipesCommon.MetaBlock metaBlock;

	public BlockMutatorDebris(Block block) {
		this(block, 0);
	}

	public BlockMutatorDebris(Block block, int meta) {
		this.metaBlock = new RecipesCommon.MetaBlock(block, meta);
	}


	@Override
	public void mutatePre(ExplosionVNT explosion, IBlockState blockState, BlockPos pos) {

	}

	@Override
	public void mutatePost(ExplosionVNT explosion, BlockPos pos) {
		World world = explosion.world;

		for (EnumFacing dir : EnumFacing.values()) {
			IBlockState state = world.getBlockState(pos.offset(dir));
			Block adjacentBlock = state.getBlock();

			if (adjacentBlock.isNormalCube(state) && (adjacentBlock != metaBlock.block || adjacentBlock.getMetaFromState(state) != metaBlock.meta)) {
				world.setBlockState(pos, metaBlock.block.getStateFromMeta(metaBlock.meta), 3);
				return;
			}
		}
	}
}

