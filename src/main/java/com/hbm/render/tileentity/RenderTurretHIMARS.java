package com.hbm.render.tileentity;

import com.hbm.items.weapon.ItemAmmoHIMARS;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.turret.TileEntityTurretHIMARS;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderTurretHIMARS extends TileEntitySpecialRenderer<TileEntityTurretHIMARS> {
  @Override
  public void render(
      TileEntityTurretHIMARS turret,
      double x,
      double y,
      double z,
      float interp,
      int destroyStage,
      float alpha) {
    Vec3d pos = turret.byHorizontalIndexOffset();

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + pos.x, y, z + pos.z);
    GlStateManager.enableLighting();
    GlStateManager.enableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    bindTexture(ResourceManager.turret_arty_tex);
    ResourceManager.turret_arty.renderPart("Base");
    float yaw =
            (float) (-Math.toDegrees(
                            turret.lastRotationYaw + (turret.rotationYaw - turret.lastRotationYaw) * interp)
                        - 90F);
    float pitch =
            (float) Math.toDegrees(
                turret.lastRotationPitch + (turret.rotationPitch - turret.lastRotationPitch) * interp);

    bindTexture(ResourceManager.turret_himars_tex);
    GlStateManager.rotate(yaw - 90, 0, 1, 0);
    ResourceManager.turret_himars.renderPart("Carriage");

    GlStateManager.translate(0, 2.25, 2);
    GlStateManager.rotate(pitch, 1, 0, 0);
    GlStateManager.translate(0, -2.25, -2);
    ResourceManager.turret_himars.renderPart("Launcher");

    double barrel = turret.lastCrane + (turret.crane - turret.lastCrane) * interp;
    double length = -5D;
    GlStateManager.translate(0, 0, barrel * length);
    ResourceManager.turret_himars.renderPart("Crane");

    if (turret.typeLoaded >= 0) {
      ItemAmmoHIMARS.HIMARSRocket rocket = ItemAmmoHIMARS.itemTypes[turret.typeLoaded];
      bindTexture(rocket.texture);

      if (rocket.modelType == ItemAmmoHIMARS.HIMARSRocket.Type.Standard) {
        ResourceManager.turret_himars.renderPart("TubeStandard");

        for (int i = 0; i < turret.ammo; i++) {
          ResourceManager.turret_himars.renderPart("CapStandard" + (5 - i + 1));
        }
      } else if (rocket.modelType == ItemAmmoHIMARS.HIMARSRocket.Type.Single) {
        ResourceManager.turret_himars.renderPart("TubeSingle");

        if (turret.hasAmmo()) {
          ResourceManager.turret_himars.renderPart("CapSingle");
        }
      }
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.popMatrix();
  }
}
