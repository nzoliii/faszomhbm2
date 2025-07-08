package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityArtilleryRocket;
import com.hbm.items.weapon.ItemAmmoHIMARS.HIMARSRocket;
import com.hbm.main.ResourceManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

public class RenderArtilleryRocket extends Render<EntityArtilleryRocket> {

  public static final IRenderFactory<EntityArtilleryRocket> FACTORY =
      man -> new RenderArtilleryRocket(man);

  protected RenderArtilleryRocket(RenderManager renderManager) {
    super(renderManager);
  }

  @Override
  public void doRender(
      EntityArtilleryRocket entity, double x, double y, double z, float f0, float f1) {

    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, z);
    GlStateManager.rotate(
        entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f1 - 90.0F,
        0.0F,
        1.0F,
        0.0F);
    GlStateManager.rotate(
        entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * f1 - 90,
        0.0F,
        0.0F,
        1.0F);
    GlStateManager.rotate(90, 0, 1, 0);
    GlStateManager.rotate(90, 1, 0, 0);

    this.bindEntityTexture(entity);

    boolean fog = GL11.glIsEnabled(GL11.GL_FOG);

    if (fog) GlStateManager.disableFog();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);
    HIMARSRocket rocket = entity.getType();
    if (rocket.modelType == HIMARSRocket.Type.Standard)
      ResourceManager.turret_himars.renderPart("RocketStandard");
    if (rocket.modelType == HIMARSRocket.Type.Single)
      ResourceManager.turret_himars.renderPart("RocketSingle");
    GlStateManager.shadeModel(GL11.GL_FLAT);
    if (fog) GlStateManager.enableFog();

    GlStateManager.popMatrix();
  }

  @Override
  protected ResourceLocation getEntityTexture(EntityArtilleryRocket entity) {
    return entity.getType().texture;
  }
}
