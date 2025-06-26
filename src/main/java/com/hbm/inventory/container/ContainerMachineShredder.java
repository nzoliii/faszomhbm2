package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.tileentity.machine.TileEntityMachineShredder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerMachineShredder extends Container {

	private final TileEntityMachineShredder shredder;
	private int progress;
	
	public ContainerMachineShredder(InventoryPlayer invPlayer, TileEntityMachineShredder teMachineShredder) {

		shredder = teMachineShredder;
		
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 0, 44, 18));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 1, 62, 18));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 2, 80, 18));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 3, 44, 36));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 4, 62, 36));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 5, 80, 36));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 6, 44, 54));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 7, 62, 54));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 8, 80, 54));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 9, 116, 18));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 10, 134, 18));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 11, 152, 18));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 12, 116, 36));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 13, 134, 36));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 14, 152, 36));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 15, 116, 54));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 16, 134, 54));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 17, 152, 54));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 18, 116, 72));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 19, 134, 72));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 20, 152, 72));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 21, 116, 90));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 22, 134, 90));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 23, 152, 90));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 24, 116, 108));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 25, 134, 108));
		this.addSlotToContainer(new SlotMachineOutput(shredder.inventory, 26, 152, 108));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 27, 44, 108));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 28, 80, 108));
		this.addSlotToContainer(new SlotItemHandler(shredder.inventory, 29, 8, 108));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 67));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + 67));
		}
	}
	
	@Override
	public void addListener(@NotNull IContainerListener listener) {
		super.addListener(listener);
		listener.sendWindowProperty(this, 1, shredder.progress);
	}
	
	@Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index)
    {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack stack = slot.getStack();
			rStack = stack.copy();
			
            if (index <= 29) {
				if (!this.mergeItemStack(stack, 30, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else
			{
				if (!this.mergeItemStack(stack, 0, 9, false))
					if (!this.mergeItemStack(stack, 27, 30, false))
						return ItemStack.EMPTY;
			}
			
			if (stack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		
		return rStack;
    }

	@Override
	public boolean canInteractWith(@NotNull EntityPlayer player) {
		return shredder.isUseableByPlayer(player);
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();

        for (IContainerListener listener : this.listeners) {
            if (this.progress != this.shredder.progress) {
				listener.sendWindowProperty(this, 1, this.shredder.progress);
            }
        }

		this.progress = this.shredder.progress;
	}
	
	@Override
	public void updateProgressBar(int i, int j) {
		if(i == 1)
		{
			shredder.progress = j;
		}
	}
}
