package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import org.jetbrains.annotations.NotNull;

public class RenderArcWelder extends TileEntitySpecialRenderer<TileEntityMachineArcWelder> {
    @Override
    public void render(@NotNull TileEntityMachineArcWelder arc_welder, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y, z + 0.5);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch(arc_welder.getBlockMetadata() - BlockDummyable.offset) {
            case 2: GlStateManager.rotate(90, 0F, 1F, 0F); break;
            case 4: GlStateManager.rotate(180, 0F, 1F, 0F); break;
            case 3: GlStateManager.rotate(270, 0F, 1F, 0F); break;
            case 5: GlStateManager.rotate(0, 0F, 1F, 0F); break;
        }

        GlStateManager.translate(-0.5, 0, 0);

        bindTexture(ResourceManager.arc_welder_tex);
        ResourceManager.arc_welder.renderAll();

        if(arc_welder.display != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0625D * 2.5D, 1.125D, 0D);
            GlStateManager.enableLighting();
            GlStateManager.rotate(90, 0F, 1F, 0F);
            GlStateManager.rotate(-90, 1F, 0F, 0F);

            if(arc_welder.display != null) {
                //ItemStack stack = arc_welder.display.copy();

                //EntityItem item = new EntityItem(null, 0.0D, 0.0D, 0.0D, stack);
                //item.stackSize = 1;
                //item.hoverStart = 0.0F;

                //RenderItem.renderInFrame = true;
                GlStateManager.scale(1.5, 1.5, 1.5);
                //this.itemRenderer.doRender(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
                //RenderItem.renderInFrame = false;
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }
}
