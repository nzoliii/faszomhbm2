package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.SolidificationRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class SolidificationHandler extends JEIUniversalHandler {

    public SolidificationHandler(IGuiHelper helper) {
        super(helper, JEIConfig.SOLIDIFICATION, ModBlocks.machine_solidifier.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_solidifier)}, wrap(SolidificationRecipes.getRecipes()));
    }

    private static HashMap<Object, Object> wrap(HashMap<ItemStack, ItemStack> map) {
        return new HashMap<>(map);
    }
}
