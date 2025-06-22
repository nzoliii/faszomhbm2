package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.world.biome.Biome;
import org.lwjgl.opengl.GL11;

public class RenderLandmine extends TileEntitySpecialRenderer<TileEntityLandmine> {

	@Override
	public void render(TileEntityLandmine te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        
		GL11.glRotatef(180, 0F, 1F, 0F);

		Block block = te.getWorld().getBlockState(te.getPos()).getBlock();

		if(block == ModBlocks.mine_ap) {
			GL11.glScaled(0.375D, 0.375D, 0.375D);
			GL11.glTranslated(0, -0.0625 * 3.5, 0);
			Biome biome = te.getWorld().getBiome(te.getPos());
			if(te.getWorld().getHeight(te.getPos()).getY() > te.getPos().getY() + 2) bindTexture(ResourceManager.mine_ap_stone_tex);
			else if(biome.getEnableSnow()) bindTexture(ResourceManager.mine_ap_snow_tex);
			else if(biome.getDefaultTemperature() >= 1.5F && biome.getRainfall() <= 0.1F) bindTexture(ResourceManager.mine_ap_desert_tex);
			else bindTexture(ResourceManager.mine_ap_grass_tex);
			ResourceManager.mine_ap.renderAll();
		}
		if(block == ModBlocks.mine_he) {
			GL11.glRotatef(-90, 0F, 1F, 0F);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			bindTexture(ResourceManager.mine_marelet_tex);
			ResourceManager.mine_marelet.renderAll();
			GL11.glShadeModel(GL11.GL_FLAT);
		}
		if(block == ModBlocks.mine_shrap) {
			bindTexture(ResourceManager.mine_shrap_tex);
        	ResourceManager.mine_he.renderAll();
		}
		if(block == ModBlocks.mine_fat) {
			GL11.glScaled(0.25D, 0.25D, 0.25D);
			bindTexture(ResourceManager.mine_fat_tex);
        	ResourceManager.mine_fat.renderAll();
		}

		GlStateManager.enableCull();
        GL11.glPopMatrix();
	}
}
