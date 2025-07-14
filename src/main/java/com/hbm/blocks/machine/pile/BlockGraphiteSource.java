package com.hbm.blocks.machine.pile;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.pile.TileEntityPileSource;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockGraphiteSource extends BlockGraphiteDrilledTE implements IToolable {

	public BlockGraphiteSource(String s){
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int mets) {
		return new TileEntityPileSource();
	}

	@Override
	protected ItemStack getInsertedItem() {
		return this == ModBlocks.block_graphite_plutonium ? new ItemStack(ModItems.pile_rod_plutonium) : new ItemStack(ModItems.pile_rod_source);
	}
}