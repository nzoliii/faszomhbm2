package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.FractionRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class FractioningRecipeHandler extends JEIUniversalHandler {

	public FractioningRecipeHandler(IGuiHelper helper) {
		super(helper, JEIConfig.FRACTIONING, ModBlocks.machine_fraction_tower.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_fraction_tower)}, FractionRecipes.getFractionRecipesForJEI());
	}

}
