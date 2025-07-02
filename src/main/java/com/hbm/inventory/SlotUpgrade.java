package com.hbm.inventory;

import com.hbm.items.machine.ItemMachineUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class SlotUpgrade extends SlotItemHandler {

	public SlotUpgrade(IItemHandler inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(@NotNull ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade;
    }

	@Override
    public void onSlotChange(ItemStack sta1, ItemStack sta2) {
		super.onSlotChange(sta1, sta2);
    }
}