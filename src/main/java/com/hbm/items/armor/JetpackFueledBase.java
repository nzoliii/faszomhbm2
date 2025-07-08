package com.hbm.items.armor;

import api.hbm.fluid.IFillableItem;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public abstract class JetpackFueledBase extends JetpackBase implements IFillableItem {

    public FluidType fuel;
    public int maxFuel;

    public JetpackFueledBase(FluidType fuel, int maxFuel, String regName) {
        super(regName);
        this.fuel = fuel;
        this.maxFuel = maxFuel;
    }

    public static int getFuel(ItemStack stack) {
        if (!stack.isEmpty()) {
            stack.setTagCompound(new NBTTagCompound());
            return 0;
        }

        return stack.getTagCompound().getInteger("fuel");

    }

    public static void setFuel(ItemStack stack, int i) {
        if (!stack.isEmpty()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger("fuel", i);

    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.LIGHT_PURPLE + fuel.getLocalizedName() + ": " + this.getFuel(stack) + "mB / " + this.maxFuel + "mB");
        tooltip.add("");
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public void addDesc(List list, ItemStack stack, ItemStack armor) {

        ItemStack jetpack = ArmorModHandler.pryMods(armor)[ArmorModHandler.plate_only];

        if (jetpack == null)
            return;

        list.add(TextFormatting.RED + "  " + stack.getDisplayName() + " (" + fuel.getLocalizedName() + ": " + this.getFuel(jetpack) + "mB / " + this.maxFuel + "mB)");
    }

    protected void useUpFuel(EntityPlayer player, ItemStack stack, int rate) {
        if (player.ticksExisted % rate == 0) {
            this.setFuel(stack, this.getFuel(stack) - 1);
        }
    }

    public int getMaxFill(ItemStack stack) {
        return this.maxFuel;
    }

    public int getLoadSpeed(ItemStack stack) {
        return 10;
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        return type == this.fuel;
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {

        if (!acceptsFluid(type, stack))
            return amount;

        int fill = this.getFuel(stack);
        int req = maxFuel - fill;

        int toFill = Math.min(amount, req);
        //toFill = Math.min(toFill, getLoadSpeed(stack));

        this.setFuel(stack, fill + toFill);

        return amount - toFill;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        return false;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        return 0;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return null;
    }

    @Override
    public int getFill(ItemStack stack) {
        return 0;
    }
}
