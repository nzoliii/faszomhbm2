package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityPWRController;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerPWR extends Container {

    TileEntityPWRController controller;

    public ContainerPWR(InventoryPlayer invPlayer, TileEntityPWRController controller) {
        this.controller = controller;

        this.addSlotToContainer(new SlotItemHandler(controller.inventory, 0, 53, 5));
        this.addSlotToContainer(new SlotMachineOutput(controller.inventory, 1, 89, 32));  // Output slot
        this.addSlotToContainer(new SlotItemHandler(controller.inventory, 2, 8, 59));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 106 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 164));
        }
    }

    @Override
    public ItemStack transferStackInSlot(@NotNull EntityPlayer player, int par2) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = this.inventorySlots.get(par2);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(par2 <= 2) {
                if(!this.mergeItemStack(var5, 3, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {

                if(var3.getItem() instanceof IItemFluidIdentifier) {
                    if(!this.mergeItemStack(var5, 2, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!this.mergeItemStack(var5, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
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
        return controller.isUseableByPlayer(player);
    }

}