package com.hbm.handler.jei;

import com.hbm.handler.jei.JeiRecipes.GasCentRecipe;
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
import net.minecraft.util.ResourceLocation;

public class GasCentrifugeRecipeHandler implements IRecipeCategory<GasCentRecipe> {

  public static final ResourceLocation gui_rl =
      new ResourceLocation(RefStrings.MODID, "textures/gui/jei/gui_jei_gas_centrifuge.png");

  protected final IDrawable background;
  protected final IDrawableStatic powerStatic;
  protected final IDrawableAnimated powerAnimated;
  protected final IDrawableStatic progressStatic;
  protected final IDrawableAnimated progressAnimated;

  public GasCentrifugeRecipeHandler(IGuiHelper help) {
    background = help.createDrawable(gui_rl, 3, 6, 162, 54);

    powerStatic = help.createDrawable(gui_rl, 168, 37, 16, 34);
    powerAnimated = help.createAnimatedDrawable(powerStatic, 480, StartDirection.TOP, true);

    progressStatic = help.createDrawable(gui_rl, 168, 0, 44, 37);
    progressAnimated = help.createAnimatedDrawable(progressStatic, 150, StartDirection.LEFT, false);
  }

  @Override
  public String getUid() {
    return JEIConfig.GAS_CENT;
  }

  @Override
  public String getTitle() {
    return I18nUtil.resolveKey("tile.machine_gascent.name");
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
  public void drawExtras(Minecraft minecraft) {
    powerAnimated.draw(minecraft, 1, 1);
    progressAnimated.draw(minecraft, 72, 12);
  }

  @Override
  public void setRecipe(
      IRecipeLayout recipeLayout, GasCentRecipe recipeWrapper, IIngredients ingredients) {
    IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

    guiItemStacks.init(0, true, 44, 18);

    guiItemStacks.init(1, false, 126, 9);
    guiItemStacks.init(2, false, 144, 9);
    guiItemStacks.init(3, false, 126, 27);
    guiItemStacks.init(4, false, 144, 27);

    guiItemStacks.init(5, true, 0, 36);

    guiItemStacks.set(ingredients);
    guiItemStacks.set(5, JeiRecipes.getBatteries());
  }
}
