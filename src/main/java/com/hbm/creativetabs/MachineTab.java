package com.hbm.creativetabs;

import com.hbm.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MachineTab extends CreativeTabs {

	public MachineTab(int index, String label) {
		super(index, label);
	}

	@NotNull
	@Override
	public ItemStack createIcon() {
        return new ItemStack(Item.getItemFromBlock(ModBlocks.pwr_controller));
    }
}
