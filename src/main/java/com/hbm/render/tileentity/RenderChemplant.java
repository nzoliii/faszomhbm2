package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.HmfController;
import com.hbm.tileentity.machine.TileEntityMachineChemplant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

public class RenderChemplant extends TileEntitySpecialRenderer<TileEntityMachineChemplant> {

	@Override
	public void render(TileEntityMachineChemplant te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(180, 0F, 1F, 0F);
		switch(te.getBlockMetadata() - BlockDummyable.offset) {
			case 5:
				GL11.glRotatef(180, 0F, 1F, 0F);
				break;
			case 2:
				GL11.glRotatef(270, 0F, 1F, 0F);
				break;
			case 4:
				GL11.glRotatef(0, 0F, 1F, 0F);
				break;
			case 3:
				GL11.glRotatef(90, 0F, 1F, 0F);
				break;
		}

		GL11.glTranslated(-0.5D, 0.0D, 0.5D);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.chemplant_body_tex);
		ResourceManager.chemplant_body.renderAll();
		GL11.glShadeModel(GL11.GL_FLAT);

        GL11.glPopMatrix();
        
        renderExtras(te, x, y, z, partialTicks);
	}
	
	public void renderExtras(TileEntity tileEntity, double x, double y, double z, float f) {
        GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glRotatef(180, 0F, 1F, 0F);
		TileEntityMachineChemplant chem = (TileEntityMachineChemplant) tileEntity;
		switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
			case 5:
				GL11.glRotatef(180, 0F, 1F, 0F);
				break;
			case 2:
				GL11.glRotatef(270, 0F, 1F, 0F);
				break;
			case 4:
				GL11.glRotatef(0, 0F, 1F, 0F);
				break;
			case 3:
				GL11.glRotatef(90, 0F, 1F, 0F);
				break;
		}
		
		bindTexture(ResourceManager.chemplant_spinner_tex);

        int rotation = (int) (System.currentTimeMillis() % (360 * 5)) / 5;

        GL11.glPushMatrix();
		GL11.glTranslated(-0.625, 0, 0.625);
		
		if(chem.tanksNew[0].getTankType() != null && chem.isProgressing)
			GL11.glRotatef(-rotation, 0F, 1F, 0F);
		else
			GL11.glRotatef(-45, 0F, 1F, 0F);
		
		ResourceManager.chemplant_spinner.renderAll();
        GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(-0.625, 0, 0.625);
		
		if(chem.tanksNew[1].getTankType() != null && chem.isProgressing)
			GL11.glRotatef(rotation, 0F, 1F, 0F);
		else
			GL11.glRotatef(45, 0F, 1F, 0F);
		
		ResourceManager.chemplant_spinner.renderAll();
        GL11.glPopMatrix();

        double push = Math.sin((System.currentTimeMillis() % 2000) / 1000D * Math.PI) * 0.25 - 0.25;

        bindTexture(ResourceManager.chemplant_piston_tex);
        
        GL11.glPushMatrix();
        
        if(chem.isProgressing)
        	GL11.glTranslated(0, push, 0);
		else
        	GL11.glTranslated(0, -0.25, 0);
        
        ResourceManager.chemplant_piston.renderAll();
        GL11.glPopMatrix();

        bindTexture(ResourceManager.chemplant_fluid_tex);
		int color = 0;

        GlStateManager.disableLighting();
		if(chem.tanksNew[0].getTankType() != Fluids.NONE) {
			GL11.glPushMatrix();

			if(chem.isProgressing)
				HmfController.setMod(50000D, -250D);
			else
				HmfController.setMod(50000D, -50000D);

			color = chem.tanksNew[0].getTankType().getColor();
			GL11.glColor3ub((byte) ((color & 0xFF0000) >> 16), (byte) ((color & 0x00FF00) >> 8), (byte) ((color & 0x0000FF) >> 0));
			GL11.glTranslated(-0.625, 0, 0.625);

			int count = chem.tanksNew[0].getFill() * 16 / 24000;
			for(int i = 0; i < count; i++) {

				if(i < count - 1)
					ResourceManager.chemplant_fluid.renderAll();
				else
					ResourceManager.chemplant_fluidcap.renderAll();
				GL11.glTranslated(0, 0.125, 0);
			}
			GL11.glPopMatrix();
		}

		if(chem.tanksNew[1].getTankType() != Fluids.NONE) {
			GL11.glPushMatrix();

			if(chem.isProgressing)
				HmfController.setMod(50000D, 250D);
			else
				HmfController.setMod(50000D, 50000D);

			color = chem.tanksNew[1].getTankType().getColor();
			GL11.glColor3ub((byte) ((color & 0xFF0000) >> 16), (byte) ((color & 0x00FF00) >> 8), (byte) ((color & 0x0000FF) >> 0));
			GL11.glTranslated(0.625, 0, 0.625);

			int count = chem.tanksNew[1].getFill() * 16 / 24000;
			for(int i = 0; i < count; i++) {

				if(i < count - 1)
					ResourceManager.chemplant_fluid.renderAll();
				else
					ResourceManager.chemplant_fluidcap.renderAll();
				GL11.glTranslated(0, 0.125, 0);
			}
			GL11.glPopMatrix();
		}
        GlStateManager.enableLighting();
        
        HmfController.resetMod();

        GL11.glPopMatrix();
	}
}
