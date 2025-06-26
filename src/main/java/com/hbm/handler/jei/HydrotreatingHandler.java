package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class HydrotreatingHandler extends JEIUniversalHandler {
    public HydrotreatingHandler(IGuiHelper helper) {
        super(helper, JEIConfig.HYDROTREATING, ModBlocks.machine_hydrotreater.getTranslationKey(), new ItemStack[] { new ItemStack(ModBlocks.machine_hydrotreater) }, wrap(HydrotreatingRecipes.getRecipes()));
    }

    private static HashMap<Object, Object> wrap(HashMap<Object, Object[]> map) {
        return new HashMap<>(map);
    }
}
