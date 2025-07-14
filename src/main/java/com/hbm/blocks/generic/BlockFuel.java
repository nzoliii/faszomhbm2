package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BlockFuel extends BlockMeta {

	public int encouragement;
	public int flammability;
	
	public BlockFuel(Material m, String s, int en, int flam){
		super(m, s);
		this.encouragement = en;
		this.flammability = flam;
	}

	public BlockFuel(Material m, String s, int en, int flam, SoundType type, short metaCount){
		super(m, type, s, metaCount);
		this.encouragement = en;
		this.flammability = flam;
	}
	
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face){
		return flammability;
	}
	
	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face){
		return encouragement;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face){
		return true;
	}

	@Override
	public boolean isFireSource(World world, BlockPos pos, EnumFacing side){
		return true;
	}

	@Override
	public void registerItem() {
		ItemBlock itemBlock = new BlockFuelMetaItem(this);
		itemBlock.setRegistryName(this.getRegistryName());
		if (getShowMetaInCreative()) itemBlock.setCreativeTab(this.getCreativeTab());
		ForgeRegistries.ITEMS.register(itemBlock);
	}

	public static class BlockFuelMetaItem extends BlockMeta.MetaBlockItem {
		public BlockFuelMetaItem(Block block) {
			super(block);
		}

		@Override
		public String getTranslationKey(ItemStack stack) {
			return this.block.getTranslationKey();
		}
	}
}
