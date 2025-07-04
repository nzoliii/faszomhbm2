package com.hbm.inventory.fluid.tank;

import api.hbm.fluid.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class FluidLoaderFillableItem implements IFluidLoadingHandler {

    @Override
    public boolean fillItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        return fill(slots.getStackInSlot(in), tank);
    }

    public boolean fill(ItemStack stack, FluidTankNTM tank) {

        if (tank.pressure != 0) return false;

        if (stack == null || stack.isEmpty()) return false;

        FluidType type = tank.getTankType();

        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            for (ItemStack mod : ArmorModHandler.pryMods(stack)) {

                if (mod != null && mod.getItem() instanceof IFillableItem) {
                    fill(mod, tank);
                }
            }
        }

        if (!(stack.getItem() instanceof IFillableItem fillable)) return false;

        if (fillable.acceptsFluid(type, stack)) {
            tank.setFill(fillable.tryFill(type, tank.getFill(), stack));
        }

        return true;
    }

    @Override
    public boolean emptyItem(IItemHandler slots, int in, int out, FluidTankNTM tank) {
        return empty(slots.getStackInSlot(in), tank);
    }

    public boolean empty(ItemStack stack, FluidTankNTM tank) {
        if (stack == null || stack.isEmpty()) return false;
        boolean success = false;
        if (stack.getItem() instanceof ItemArmor && ArmorModHandler.hasMods(stack)) {
            for (ItemStack mod : ArmorModHandler.pryMods(stack)) {
                if (empty(mod, tank)) success = true;
            }
        }
        if (!(stack.getItem() instanceof IFillableItem fillable)) {
            return success;
        }

        FluidType fluidToTransfer = null;
        if (tank.getTankType() == Fluids.NONE) {
            FluidType itemFluidType = fillable.getFirstFluidType(stack);
            if (itemFluidType != null && itemFluidType != Fluids.NONE) {
                tank.setTankType(itemFluidType);
                fluidToTransfer = itemFluidType;
            }
        } else {
            if (fillable.providesFluid(tank.getTankType(), stack)) {
                fluidToTransfer = tank.getTankType();
            }
        }

        if (fluidToTransfer != null) {
            int spaceInTank = tank.getMaxFill() - tank.getFill();
            if (spaceInTank > 0) {
                int amountEmptied = fillable.tryEmpty(fluidToTransfer, spaceInTank, stack);
                if (amountEmptied > 0) {
                    tank.setFill(tank.getFill() + amountEmptied);
                    success = true;
                }
            }
        }

        return success;
    }
}