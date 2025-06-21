package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class RenderRadGen extends TileEntitySpecialRenderer<TileEntityMachineRadGen> {

	@Override
	public boolean isGlobalRenderer(@NotNull TileEntityMachineRadGen te) {
		return true;
	}
	
	@Override
	public void render(TileEntityMachineRadGen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);

		switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
			case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.radgen_body_tex);

		ResourceManager.radgen_body.renderPart("Base");

		GL11.glPushMatrix();
		if(te.isOn) {
			GL11.glTranslated(0, 1.5, 0);
			GL11.glRotatef((System.currentTimeMillis() % 3600) * -0.1F, 1F, 0F, 0F);
			GL11.glTranslated(0, -1.5, 0);
		}
		ResourceManager.radgen_body.renderPart("Rotor");
		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
		if(te.isOn)
			GL11.glColor3f(0F, 1F, 0F);
		else
			GL11.glColor3f(0F, 0.1F, 0F);
		ResourceManager.radgen_body.renderPart("Light");
		GL11.glColor3f(1F, 1F, 1F);
		GlStateManager.enableLighting();
		GL11.glPopAttrib();

		int brightness = te.getWorld().getCombinedLight(new BlockPos(te.getPos()), 0);
		int lX = brightness % 65536;
		int lY = brightness / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) lX, (float) lY);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(false);

		GL11.glColor4f(0.5F, 0.75F, 1F, 0.3F);
		ResourceManager.radgen_body.renderPart("Glass");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glDepthMask(true);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDisable(GL11.GL_BLEND);

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		ResourceManager.radgen_body.renderPart("Glass");

		GL11.glShadeModel(GL11.GL_FLAT);

		GL11.glPopMatrix();
	}
}
