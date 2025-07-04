package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.LiquefactionRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class LiquefactionHandler extends JEIUniversalHandler {
    public LiquefactionHandler(IGuiHelper helper) {
        super(helper, JEIConfig.LIQUEFACTION, ModBlocks.machine_liquefactor.getTranslationKey(), new ItemStack[] { new ItemStack(ModBlocks.machine_liquefactor) }, wrap(LiquefactionRecipes.getRecipes()));
    }

    private static HashMap<Object, Object> wrap(HashMap<Object, ItemStack> map) {
        return new HashMap<>(map);
    }
}
