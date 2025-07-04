package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CrystallizerRecipeHandler extends JEIUniversalHandler {

	@Override
	protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
		for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
			ItemStack[] inputs = extractInput(entry.getKey());
			if (inputs.length < 2) continue;

			ItemStack acidStack = inputs[0];
			ItemStack itemStack = inputs[1];

			FluidType fluidType = null;
			if (acidStack.getItem() instanceof ItemFluidIcon) {
				fluidType = ItemFluidIcon.getFluidType(acidStack);
			}

			if (fluidType == null) continue;

			CrystallizerRecipes.CrystallizerRecipe originalRecipe = CrystallizerRecipes.getOutput(itemStack, fluidType);

			if (originalRecipe == null) continue;

			ItemStack output = originalRecipe.output.copy();
			output.setCount(output.getCount() * (int)(1 + originalRecipe.productivity));

			int productivity = (int)(originalRecipe.productivity * 100);

			recipes.add(new JeiRecipes.CrystallizerRecipe(
					inputs,
					new ItemStack[]{output},
					machines,
					productivity
			));
		}
	}


	public CrystallizerRecipeHandler(IGuiHelper helper) {
		super(helper, JEIConfig.CRYSTALLIZER, ModBlocks.machine_crystallizer.getTranslationKey(), new ItemStack[]{new ItemStack(ModBlocks.machine_crystallizer)}, CrystallizerRecipes.getRecipes());
	}

}
