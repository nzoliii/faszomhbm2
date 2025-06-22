package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineRadGen extends Container {
	
	private TileEntityMachineRadGen diFurnace;
	
	public ContainerMachineRadGen(InventoryPlayer invPlayer, TileEntityMachineRadGen tedf) {
		diFurnace = tedf;

		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotItemHandler(tedf.inventory, j + i * 3, 8 + j * 18, 17 + i * 18));
			}
		}
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlotToContainer(new SlotMachineOutput(tedf.inventory, j + i * 3 + 12, 116 + j * 18, 17 + i * 18));
			}
		}

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 160));
		}
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer p_82846_1_, int par2) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.inventorySlots.get(par2);

		if(var4 != null && var4.getHasStack()) {
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();

			if(par2 <= 23) {
				if(!this.mergeItemStack(var5, 24, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(var5, 0, 12, false)) {
				return ItemStack.EMPTY;
			}

			if(var5.getCount() == 0) {
				var4.putStack(ItemStack.EMPTY);
			} else {
				var4.onSlotChanged();
			}
		}

		return var3;
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return diFurnace.isUseableByPlayer(player);
	}
}