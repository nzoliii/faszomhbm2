package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ITooltipProvider;

import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.items.IDynamicModels;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockPillarPWR extends BlockBakeBase implements ITooltipProvider, IDynamicModels {

    public BlockPillarPWR(Material mat, String name, String top, String side) {
        super(mat, name, top, side);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        this.addStandardInfo(tooltip);
        super.addInformation(stack, player, tooltip, advanced);
    }
}