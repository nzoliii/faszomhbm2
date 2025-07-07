package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;

public class RenderAutosaw extends TileEntitySpecialRenderer<TileEntityMachineAutosaw> {

  @Override
  public void render(
      @NotNull TileEntityMachineAutosaw saw,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {
    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5D, y, z + 0.5D);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();

    float turn = saw.prevRotationYaw + (saw.rotationYaw - saw.prevRotationYaw) * partialTicks;
    float angle =
        80 - (saw.prevRotationPitch + (saw.rotationPitch - saw.prevRotationPitch) * partialTicks);
    float spin = saw.lastSpin + (saw.spin - saw.lastSpin) * partialTicks;
    double engine =
        saw.isOn
            ? Math.sin(saw.getWorld().getTotalWorldTime() * 2 % (Math.PI * 2) + partialTicks)
            : 0;
    renderCommon(turn, angle, spin, engine);

    GlStateManager.popMatrix();
  }

  public void renderCommon(float turn, float angle, float spin, double engine) {

    bindTexture(ResourceManager.autosaw_tex);
    ResourceManager.autosaw.renderPart("Base");

    GlStateManager.rotate(turn, 0, -1, 0);
    ResourceManager.autosaw.renderPart("Main");
    GlStateManager.pushMatrix();
    GlStateManager.translate(0, engine * 0.01, 0);
    ResourceManager.autosaw.renderPart("Engine");
    GlStateManager.popMatrix();

    GlStateManager.translate(0, 1.75, 0);
    GlStateManager.rotate(angle, 1, 0, 0);
    GlStateManager.translate(0, -1.75, 0);
    ResourceManager.autosaw.renderPart("ArmUpper");

    GlStateManager.translate(0, 1.75, -4);
    GlStateManager.rotate(angle * -2, 1, 0, 0);
    GlStateManager.translate(0, -1.75, 4);
    GlStateManager.translate(-0.01, 0, 0);
    ResourceManager.autosaw.renderPart("ArmLower");
    GlStateManager.translate(0.01, 0, 0);

    GlStateManager.translate(0, 1.75, -8);
    GlStateManager.rotate(angle, 1, 0, 0);
    GlStateManager.translate(0, -1.75, 8);
    ResourceManager.autosaw.renderPart("ArmTip");

    GlStateManager.translate(0, 1.75, -10);
    GlStateManager.rotate(spin, 0, -1, 0);
    GlStateManager.translate(0, -1.75, 10);
    ResourceManager.autosaw.renderPart("Sawblade");
  }
}
