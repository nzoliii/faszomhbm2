package com.hbm.handler.jei;


import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.RBMKOutgasserRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class RBMKOutgasserRecipeHandler extends JEIUniversalHandler {
	
	public RBMKOutgasserRecipeHandler(IGuiHelper helper) {
		super(helper, JEIConfig.RBMKOUTGASSER, ModBlocks.rbmk_outgasser.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.rbmk_outgasser)}, wrap(RBMKOutgasserRecipes.getRecipes()));
	}

	private static HashMap<Object, Object> wrap(HashMap<Object, Object[]> map) {
		return new HashMap<>(map);
	}

}
