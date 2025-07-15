package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class RenderIndustrialBoiler
    extends TileEntitySpecialRenderer<TileEntityHeatBoilerIndustrial> {

  @Override
  public void render(
      @NotNull TileEntityHeatBoilerIndustrial boiler,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.boiler_industrial_tex);
    ResourceManager.boiler_industrial.renderAll();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }
}
