package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class ContainerTurretBase extends Container {

  private final TileEntityTurretBaseNT turret;

  public ContainerTurretBase(InventoryPlayer invPlayer, TileEntityTurretBaseNT te) {
    turret = te;

    this.addSlotToContainer(new SlotItemHandler(te.inventory, 0, 98, 27));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        this.addSlotToContainer(
            new SlotItemHandler(te.inventory, 1 + i * 3 + j, 80 + j * 18, 63 + i * 18));
      }
    }

    this.addSlotToContainer(new SlotItemHandler(te.inventory, 10, 152, 99));

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 9; j++) {
        this.addSlotToContainer(
            new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + (18 * 3) + 2));
      }
    }

    for (int i = 0; i < 9; i++) {
      this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142 + (18 * 3) + 2));
    }
  }

  @Override
  public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int index) {
    ItemStack rStack = ItemStack.EMPTY;
    Slot slot = this.inventorySlots.get(index);

    if (slot != null && slot.getHasStack()) {
      ItemStack stack = slot.getStack();
      rStack = stack.copy();

      if (index <= turret.inventory.getSlots() - 1) {
        if (!this.mergeItemStack(
            stack, turret.inventory.getSlots(), this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else if (stack.getItem() == ModItems.turret_chip) {

        if (!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;

      } else if (!this.mergeItemStack(stack, 1, turret.inventory.getSlots(), false)) {
        return ItemStack.EMPTY;
      }

      if (stack.isEmpty()) {
        slot.putStack(ItemStack.EMPTY);
      } else {
        slot.onSlotChanged();
      }

      slot.onTake(player, stack);
    }

    return rStack;
  }

  @Override
  public boolean canInteractWith(@NotNull EntityPlayer player) {
    return turret.isUseableByPlayer(player);
  }
}
