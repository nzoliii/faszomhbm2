package com.hbm.particle;

import com.hbm.interfaces.Untested;
import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

@Untested
public class ParticleMukeWave extends Particle {

    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
    private float waveScale = 45F;

    public ParticleMukeWave(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.particleMaxAge = 25;
    }

    public ParticleMukeWave(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }

    public void setup(float scale, int maxAge) {
        this.waveScale = scale;
        this.particleMaxAge = maxAge;
    }

    public void renderParticle(BufferBuilder buf, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableCull();
        RenderHelper.disableStandardItemLighting();

        boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
        if (fog) GL11.glDisable(GL11.GL_FOG);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buffer = tess.getBuffer();

        float ageRatio = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        ageRatio = MathHelper.clamp(ageRatio, 0.0F, 1.0F);
        float alpha = 1.0F - ageRatio;
        float scale = (1 - (float)Math.pow(Math.E, (this.particleAge + partialTicks) * -0.125)) * waveScale;

        float pX = (float)(this.prevPosX + (this.posX - this.prevPosX) * partialTicks - interpPosX);
        float pY = (float)(this.prevPosY + (this.posY - this.prevPosY) * partialTicks - interpPosY);
        float pZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks - interpPosZ);

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

// Normal: (0, 1, 0)
        buffer.pos(pX - 1 * scale, pY - 0.25, pZ - 1 * scale).tex(1, 1).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
        buffer.pos(pX - 1 * scale, pY - 0.25, pZ + 1 * scale).tex(1, 0).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
        buffer.pos(pX + 1 * scale, pY - 0.25, pZ + 1 * scale).tex(0, 0).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();
        buffer.pos(pX + 1 * scale, pY - 0.25, pZ - 1 * scale).tex(0, 1).color(1F, 1F, 1F, alpha).normal(0, 1, 0).endVertex();

        tess.draw();

// Restore GL state
        GlStateManager.doPolygonOffset(0,0);
        GlStateManager.enableCull();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableLighting();
        if (fog) GL11.glEnable(GL11.GL_FOG);
    }

    public int getFXLayer(){
        return 3;
    }

}
