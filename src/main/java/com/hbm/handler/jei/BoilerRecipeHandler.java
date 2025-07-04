package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.jei.JeiRecipes.BoilerRecipe;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.lib.RefStrings;

import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.IDrawableAnimated.StartDirection;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class BoilerRecipeHandler extends JEIUniversalHandler {
	
	public BoilerRecipeHandler(IGuiHelper help) {
		super(help, JEIConfig.BOILER, ModBlocks.machine_boiler_off.getTranslationKey(), new ItemStack[]{
				new ItemStack(ModBlocks.machine_boiler_off), new ItemStack(ModBlocks.machine_boiler_electric_off),
				new ItemStack(ModBlocks.machine_boiler_rtg_off), new ItemStack(ModBlocks.machine_solar_boiler),
				new ItemStack(ModBlocks.rbmk_boiler), new ItemStack(ModBlocks.heat_boiler)
		}, generateRecipes());
	}

	public static HashMap<Object, Object> cache;
	public static boolean isReload=false;

	public static HashMap<Object, Object> generateRecipes() {

		if(cache != null && !isReload) return cache;

		cache = new HashMap();

		for(FluidType type : Fluids.getInNiceOrder()) {

			if(type.hasTrait(FT_Heatable.class)) {
				FT_Heatable trait = type.getTrait(FT_Heatable.class);

				if(trait.getEfficiency(FT_Heatable.HeatingType.BOILER) > 0) {
					FT_Heatable.HeatingStep step = trait.getFirstStep();
					cache.put(ItemFluidIcon.make(type, step.amountReq), ItemFluidIcon.make(step.typeProduced, step.amountProduced));
				}
			}
		}
		isReload=false;
		return cache;
	}

}
