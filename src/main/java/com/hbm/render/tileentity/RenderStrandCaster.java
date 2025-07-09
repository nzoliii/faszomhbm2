package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import java.nio.DoubleBuffer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class RenderStrandCaster extends TileEntitySpecialRenderer<TileEntityMachineStrandCaster> {
  public static final ResourceLocation lava =
      new ResourceLocation(RefStrings.MODID, "textures/models/machines/lava_gray.png");
  private static DoubleBuffer buf = null;

  @Override
  public void render(
      @NotNull TileEntityMachineStrandCaster caster,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float alpha) {

    if (buf == null) {
      buf = GLAllocation.createDirectByteBuffer(8 * 4).asDoubleBuffer();
    }

    GlStateManager.pushMatrix();
    GlStateManager.translate(x + 0.5, y, z + 0.5);
    switch (caster.getBlockMetadata() - BlockDummyable.offset) {
      case 4:
        GlStateManager.rotate(90, 0F, 1F, 0F);
        break;
      case 3:
        GlStateManager.rotate(180, 0F, 1F, 0F);
        break;
      case 5:
        GlStateManager.rotate(270, 0F, 1F, 0F);
        break;
      case 2:
        GlStateManager.rotate(0, 0F, 1F, 0F);
        break;
    }
    GlStateManager.translate(0.5, 0, 0.5);
    GlStateManager.rotate(180, 0, 1, 0);

    GlStateManager.enableLighting();
    GlStateManager.disableCull();
    GlStateManager.shadeModel(GL11.GL_SMOOTH);

    bindTexture(ResourceManager.strand_caster_tex);
    ResourceManager.strand_caster.renderPart("caster");

    if (caster.amount != 0 && caster.getInstalledMold() != null) {

      double level = ((double) caster.amount / (double) caster.getCapacity()) * 0.675D;
      double offset =
          ((double) caster.amount / (double) caster.getInstalledMold().getCost()) * 0.375D;

      int color = caster.type.moltenColor;

      int r = color >> 16 & 0xFF;
      int g = color >> 8 & 0xFF;
      int b = color & 0xFF;

      GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
      GlStateManager.disableLighting();

      GlStateManager.pushMatrix();
      GlStateManager.color(r / 255F, g / 255F, b / 255F);
      GL11.glEnable(GL11.GL_CLIP_PLANE0);
      buf.put(new double[] {0, 0, -1, 0.5});
      buf.rewind();
      GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);
      GlStateManager.translate(0, 0, Math.max(-offset + 3.4, 0));
      ResourceManager.strand_caster.renderPart("plate");
      GL11.glDisable(GL11.GL_CLIP_PLANE0);
      GlStateManager.popMatrix();

      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();

      buffer.normal(0F, 1F, 0F);
      buffer.color(r / 255F, g / 255F, b / 255F, 1.0F);
      bindTexture(lava);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
      buffer.pos(-0.9, 2.3 + level, -0.999).tex(0, 0).endVertex();
      buffer.pos(-0.9, 2.3 + level, 0.999).tex(0, 1).endVertex();
      buffer.pos(0.9, 2.3 + level, 0.999).tex(1, 1).endVertex();
      buffer.pos(0.9, 2.3 + level, -0.999).tex(1, 0).endVertex();
      tessellator.draw();

      GlStateManager.popMatrix();
      GlStateManager.enableLighting();
      GL11.glPopAttrib();
    }

    GlStateManager.shadeModel(GL11.GL_FLAT);

    GlStateManager.popMatrix();
  }
}
