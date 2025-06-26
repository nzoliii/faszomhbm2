package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.CokerRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class CokingRecipeHandler extends JEIUniversalHandler {

    public CokingRecipeHandler(IGuiHelper helper) {
        super(helper, JEIConfig.COKER, ModBlocks.machine_coker.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_coker)}, wrap(CokerRecipes.getRecipes()));
    }

    private static HashMap<Object, Object> wrap(HashMap<ItemStack, ItemStack[]> map) {
        return new HashMap<>(map);
    }
}
