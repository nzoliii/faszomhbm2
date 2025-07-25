package com.hbm.blocks.generic;

import com.hbm.api.block.IDrillInteraction;
import com.hbm.api.block.IMiningDrill;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockBedrockOre extends Block implements IDrillInteraction {

	public BlockBedrockOre(String s) {
		super(Material.ROCK);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean canBreak(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {
		return drill.getDrillRating() > 70;
	}

	@Override
	public ItemStack extractResource(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {

		if(drill.getDrillRating() > 70)
			return null;

		Item drop = this.getDrop();

		if(drop == null)
			return null;

		return world.rand.nextInt(50) == 0 ? new ItemStack(drop) : null;
	}

	@Override
	public float getRelativeHardness(World world, int x, int y, int z, IBlockState state, IMiningDrill drill) {
		return 30;
	}

	private Item getDrop() {

		if(this == ModBlocks.ore_bedrock_coltan)
			return ModItems.fragment_coltan;

		return null;
	}
}