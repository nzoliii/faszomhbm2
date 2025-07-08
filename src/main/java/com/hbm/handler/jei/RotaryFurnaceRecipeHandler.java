package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class RotaryFurnaceRecipeHandler extends JEIUniversalHandler {
  public RotaryFurnaceRecipeHandler(IGuiHelper helper) {
    super(
        helper,
        JEIConfig.ROTARY_FURNACE,
        ModBlocks.machine_rotary_furnace.getTranslationKey(),
        new ItemStack[] {new ItemStack(ModBlocks.machine_rotary_furnace)},
        RotaryFurnaceRecipes.getRecipes());
  }
}
