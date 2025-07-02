package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.SolderingRecipes;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

public class SolderingStationRecipeHandler extends JEIUniversalHandler {
  public SolderingStationRecipeHandler(IGuiHelper helper) {
    super(
        helper,
        JEIConfig.SOLDERING_STATION,
        ModBlocks.machine_soldering_station.getTranslationKey(),
        new ItemStack[] {new ItemStack(ModBlocks.machine_soldering_station)},
        SolderingRecipes.getRecipes());
  }
}
