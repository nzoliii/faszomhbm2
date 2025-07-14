package com.hbm.api.block;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public interface IInsertable {
    public boolean insertItem(World world, int x, int y, int z, EnumFacing dir, ItemStack stack);
}
