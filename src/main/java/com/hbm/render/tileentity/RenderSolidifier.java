package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.lwjgl.opengl.GL11;

public class RenderSolidifier extends TileEntitySpecialRenderer<TileEntityMachineSolidifier> {

    @Override
    public void render(TileEntityMachineSolidifier liq, double x, double y, double z, float f, int destroyStage, float alpha) {

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5D, y, z + 0.5D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        switch(liq.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
            case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
            case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
            case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
        }

        GL11.glShadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.solidifier_tex);
        ResourceManager.solidifier.renderPart("Main");

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        if(liq.tank.getFluidAmount() > 0 && liq.tank.getFluid() != null) {
            FFUtils.setRGBFromHex(ModForgeFluids.getFluidColor(liq.tank.getFluid().getFluid()));
            double height = (double)liq.tank.getFluidAmount() / (double)liq.tank.getCapacity();
            GL11.glPushMatrix();
            GL11.glTranslated(0, 1.25, 0);
            GL11.glScaled(1, height, 1);
            GL11.glTranslated(0, -1.25, 0);
            ResourceManager.solidifier.renderPart("Fluid");
            GL11.glPopMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(0.75F, 1.0F, 1.0F, 0.15F);
        GL11.glDepthMask(false);

        ResourceManager.solidifier.renderPart("Glass");

        GL11.glDepthMask(true);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
