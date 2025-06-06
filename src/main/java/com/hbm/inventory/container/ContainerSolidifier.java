package com.hbm.inventory.container;

import com.hbm.inventory.SlotUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerSolidifier extends Container {

    private TileEntityMachineSolidifier solidifier;

    public ContainerSolidifier(InventoryPlayer playerInv, TileEntityMachineSolidifier tile) {
        solidifier = tile;

        //Input
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 0, 71, 54));
        //Battery
        this.addSlotToContainer(new SlotItemHandler(tile.inventory, 1, 134, 72));
        //Upgrades
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 2, 98, 36));
        this.addSlotToContainer(new SlotUpgrade(tile.inventory, 3, 98, 54));

        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 122 + i * 18));
            }
        }

        for(int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 180));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return solidifier.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack var3 = ItemStack.EMPTY;
        Slot var4 = (Slot) this.inventorySlots.get(index);

        if(var4 != null && var4.getHasStack()) {
            ItemStack var5 = var4.getStack();
            var3 = var5.copy();

            if(index <= 4) {
                if(!this.mergeItemStack(var5, 5, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(!this.mergeItemStack(var5, 0, 4, false)) {
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
}
