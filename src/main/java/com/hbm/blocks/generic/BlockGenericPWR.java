package com.hbm.blocks.generic;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.IDynamicModels;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class BlockGenericPWR extends BlockBakeBase implements ITooltipProvider, IDynamicModels {
    public BlockGenericPWR(Material mat, String name, String texture) {
        super(mat, name, texture);
    }

    public BlockGenericPWR(Material mat, String name) {
        super(mat, name, name);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
        super.addInformation(stack, player, tooltip, advanced);
    }
}
