package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ReformingRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class ReformingHandler extends JEIUniversalHandler {
    public ReformingHandler(IGuiHelper helper){
        super(helper, JEIConfig.REFORMING, ModBlocks.machine_catalytic_reformer.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_catalytic_reformer)}, wrap(ReformingRecipes.getRecipes()));
    }

    private static HashMap<Object, Object> wrap(HashMap<Object, Object[]> map) {
        return new HashMap<>(map);
    }
}
