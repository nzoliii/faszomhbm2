package com.hbm.render.tileentity;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.material.Mats;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityCrucible;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class RenderCrucible extends TileEntitySpecialRenderer<TileEntityCrucible> implements ITileActorRenderer {

    public static final ResourceLocation lava = new ResourceLocation(RefStrings.MODID, "textures/models/machines/lava.png");

    @Override
    public void render(TileEntityCrucible crucible, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch (crucible.getBlockMetadata() - BlockDummyable.offset) {
            case 3 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            case 5 -> GlStateManager.rotate(0, 0F, 1F, 0F);
            case 2 -> GlStateManager.rotate(90, 0F, 1F, 0F);
            case 4 -> GlStateManager.rotate(180, 0F, 1F, 0F);
        }

        ITileActorRenderer.bindTexture(ResourceManager.crucible_tex);
        ResourceManager.crucible_heat.renderAll();

        if (!crucible.recipeStack.isEmpty() || !crucible.wasteStack.isEmpty()) {
            int totalCap = TileEntityCrucible.recipeZCapacity + TileEntityCrucible.wasteZCapacity;
            int totalMass = 0;

            for (Mats.MaterialStack stack : crucible.recipeStack)
                totalMass += stack.amount;
            for (Mats.MaterialStack stack : crucible.wasteStack)
                totalMass += stack.amount;

            double level = ((double) totalMass / (double) totalCap) * 0.875D;

            GlStateManager.pushMatrix();
            GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);

            ITileActorRenderer.bindTexture(lava);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            float yLevel = (float) (0.5 + level);

            buffer.pos(-1, yLevel, -1).tex(0, 0).endVertex();
            buffer.pos(-1, yLevel,  1).tex(0, 1).endVertex();
            buffer.pos( 1, yLevel,  1).tex(1, 1).endVertex();
            buffer.pos( 1, yLevel, -1).tex(1, 0).endVertex();

            tessellator.draw();

            GlStateManager.enableLighting();
            GL11.glPopAttrib();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

    }

    @Override
    public void renderActor(WorldInAJar world, int ticks, float partialTicks, NBTTagCompound data) {
        int x = data.getInteger("x");
        int y = data.getInteger("y");
        int z = data.getInteger("z");
        render((TileEntityCrucible) Objects.requireNonNull(world.getTileEntity(new BlockPos(x, y, z))), x, y, z, partialTicks, 0, 0);
    }

    @Override
    public void updateActor(int ticks, NBTTagCompound data) { }
}
