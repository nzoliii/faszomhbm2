package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.items.machine.ItemFluidIcon;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrystallizerRecipeHandler extends JEIUniversalHandler {

	@Override
	protected void buildRecipes(HashMap<Object, Object> recipeMap, ItemStack[] machines) {
		for (Map.Entry<Object, Object> entry : recipeMap.entrySet()) {
			List<List<ItemStack>> inputsList = extractInputLists(entry.getKey());
			if (inputsList.size() < 2) continue;
			List<ItemStack> acidStacks = inputsList.get(0);
			List<ItemStack> itemStacks = inputsList.get(1);

			if (acidStacks.isEmpty() || itemStacks.isEmpty()) continue;

			ItemStack acidStack = acidStacks.get(0);
			ItemStack itemStack = itemStacks.get(0);

			FluidType fluidType = null;
			if (acidStack.getItem() instanceof ItemFluidIcon) {
				fluidType = ItemFluidIcon.getFluidType(acidStack);
			}

			if (fluidType == null) continue;

			CrystallizerRecipes.CrystallizerRecipe originalRecipe = CrystallizerRecipes.getOutput(itemStack, fluidType);

			if (originalRecipe == null) continue;
			ItemStack output = originalRecipe.output.copy();
			int productivity = (int)(originalRecipe.productivity * 100);
			recipes.add(new JeiRecipes.CrystallizerRecipe(
					inputsList,
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