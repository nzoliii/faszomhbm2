package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderRotaryFurnace extends TileEntitySpecialRenderer<TileEntityMachineRotaryFurnace> {

  @Override
  public void render(
      TileEntityMachineRotaryFurnace furnace,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    switch (furnace.getBlockMetadata() - BlockDummyable.offset) {
      case 2:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 4:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
    }

    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    bindTexture(ResourceManager.rotary_furnace_tex);
    ResourceManager.rotary_furnace.renderPart("Furnace");
    GlStateManager.pushMatrix();

    float anim = furnace.lastAnim + (furnace.anim - furnace.lastAnim) * partialTicks;

    GlStateManager.translate(0, BobMathUtil.sps((anim * 0.75) * 0.125) * 0.5 - 0.5, 0);
    ResourceManager.rotary_furnace.renderPart("Piston");
    GlStateManager.popMatrix();
    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }
}
