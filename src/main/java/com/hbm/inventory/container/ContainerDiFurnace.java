package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;
import com.hbm.tileentity.machine.TileEntityDiFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerDiFurnace extends Container {
	private TileEntityDiFurnace diFurnace;
	public ContainerDiFurnace(InventoryPlayer invPlayer, TileEntityDiFurnace tedf) {
		diFurnace = tedf;
		
		//Inputs
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 0, 80, 18));
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 1, 80, 54));
		//Fuel
		this.addSlotToContainer(new SlotItemHandler(tedf.inventory, 2, 8, 36));
		//Output
		this.addSlotToContainer(new SlotMachineOutput(tedf.inventory, 3, 134, 36));
		
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 9; j++)
			{
				this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		if(index >= 0 && index < 3 && button == 1 && mode == ClickType.PICKUP) {
			Slot slot = this.getSlot(index);
			if(!slot.getHasStack() && player.inventory.getItemStack().isEmpty()) {
				if(!player.world.isRemote) {
					if(index == 0) diFurnace.sideUpper = (byte) ((diFurnace.sideUpper + 1) % 6);
					if(index == 1) diFurnace.sideLower = (byte) ((diFurnace.sideLower + 1) % 6);
					if(index == 2) diFurnace.sideFuel = (byte) ((diFurnace.sideFuel + 1) % 6);

					diFurnace.markDirty();
				}
				return null;
			}
		}

		return super.slotClick(index, button, mode, player);
	}
	
	//What is this!?
	//Drillgon200: ^ Literally wrote basically the same comment 10 seconds ago.
	@Override
    public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int par2)
    {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = (Slot) this.inventorySlots.get(par2);
		
		if (var4 != null && var4.getHasStack())
		{
			ItemStack var5 = var4.getStack();
			var3 = var5.copy();
			
            if (par2 <= 3) {
				if (!this.mergeItemStack(var5, 4, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(var5, 0, 3, false))
			{
				return ItemStack.EMPTY;
			}
			
			if (var5.getCount() == 0)
			{
				var4.putStack(ItemStack.EMPTY);
			}
			else
			{
				var4.onSlotChanged();
			}
		}
		
		return var3;
    }

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return diFurnace.isUsableByPlayer(player);
	}
}
