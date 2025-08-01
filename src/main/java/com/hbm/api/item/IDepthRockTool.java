package com.hbm.api.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDepthRockTool {

	/**
	 * Whether our item can break depthrock, has a couple of params so we can restrict mining for certain blocks, dimensions or positions
	 * @param world
	 * @param player
	 * @param tool
	 * @param block
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, IBlockState block, BlockPos pos);
}
