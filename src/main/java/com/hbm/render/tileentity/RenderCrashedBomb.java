package com.hbm.render.tileentity;

import com.hbm.main.ResourceManager;
import com.hbm.tileentity.bomb.TileEntityCrashedBomb;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderCrashedBomb extends TileEntitySpecialRenderer<TileEntityCrashedBomb> {

	private static final Random random = new Random();

	@Override
	public boolean isGlobalRenderer(TileEntityCrashedBomb te) {
		return true;
	}

	@Override
	public void render(TileEntityCrashedBomb te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		int choice = te.getDudChoice();
		if (choice == -1) {
			choice = 0;
		}

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GlStateManager.disableCull();
		GlStateManager.enableLighting();

		switch(te.getBlockMetadata()) {
			case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
			case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
			case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
			case 3: GL11.glRotatef(-90, 0F, 1F, 0F); break;
		}

		if (choice != 0) {
			GL11.glRotatef(60, 1F, 0F, 0F);
		}

		switch (choice) {
			case 0:
				bindTexture(ResourceManager.dud_tex);
				ResourceManager.dud.renderAll();
				break;
			case 1:
				bindTexture(ResourceManager.fhbm2_dud_balefire_tex);
				ResourceManager.fhbm2_dud_balefire.renderAll();
				break;
			case 2:
				bindTexture(ResourceManager.fhbm2_dud_conventional_tex);
				ResourceManager.fhbm2_dud_conventional.renderAll();
				break;
			case 3:
				bindTexture(ResourceManager.fhbm2_dud_nuke_tex);
				ResourceManager.fhbm2_dud_nuke.renderAll();
				break;
			case 4:
				bindTexture(ResourceManager.fhbm2_dud_salted_tex);
				ResourceManager.fhbm2_dud_salted.renderAll();
				break;
		}

		GlStateManager.enableCull();
		GL11.glPopMatrix();
	}
}