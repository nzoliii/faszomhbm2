package com.hbm.handler.jei;

import com.hbm.handler.jei.JeiRecipes.VacuumDistillRecipe;
import com.hbm.lib.RefStrings;

import com.hbm.util.I18nUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.util.ResourceLocation;

public class VacuumDistillRecipeHandler implements IRecipeCategory<VacuumDistillRecipe> {

	public static ResourceLocation gui_rl = new ResourceLocation(RefStrings.MODID + ":textures/gui/jei/gui_nei_one_to_four.png");
	
	protected final IDrawable background;
	
	public VacuumDistillRecipeHandler(IGuiHelper help) {
		background = help.createDrawable(gui_rl, 34, 34, 126, 18);
	}
	
	@Override
	public String getUid() {
		return JEIConfig.VACUUMDISTILL;
	}

	@Override
	public String getTitle() {
		return I18nUtil.resolveKey("tile.machine_vacuum_distill.name");
	}

	@Override
	public String getModName() {
		return RefStrings.MODID;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, VacuumDistillRecipe recipeWrapper, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		
		guiItemStacks.init(0, true, 0, 0);
		guiItemStacks.init(1, false, 54, 0);
		guiItemStacks.init(2, false, 72, 0);
		guiItemStacks.init(3, false, 90, 0);
		guiItemStacks.init(4, false, 108, 0);

		guiItemStacks.set(ingredients);
	}
}