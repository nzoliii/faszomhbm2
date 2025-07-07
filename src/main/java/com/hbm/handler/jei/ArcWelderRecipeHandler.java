package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class ArcWelderRecipeHandler extends JEIUniversalHandler {
  public ArcWelderRecipeHandler(IGuiHelper helper) {
    super(
        helper,
        JEIConfig.ARC_WELDER,
        ModBlocks.machine_arc_welder.getTranslationKey(),
        new ItemStack[] {new ItemStack(ModBlocks.machine_arc_welder)},
        ArcWelderRecipes.getRecipes());
  }
}
