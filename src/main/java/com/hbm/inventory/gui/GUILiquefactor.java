package com.hbm.inventory.gui;

import com.hbm.forgefluid.FFUtils;
import com.hbm.inventory.container.ContainerLiquefactor;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUILiquefactor extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_liquefactor.png");
    private TileEntityMachineLiquefactor liquefactor;

    public GUILiquefactor(InventoryPlayer invPlayer, TileEntityMachineLiquefactor tedf) {
        super(new ContainerLiquefactor(invPlayer, tedf));
        liquefactor = tedf;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 71, guiTop + 35, 16, 52, liquefactor.tank, liquefactor.fluidType);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 17, 16, 52, liquefactor.power, TileEntityMachineLiquefactor.maxPower);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.liquefactor.hasCustomInventoryName() ? this.liquefactor.getInventoryName() : I18n.format(this.liquefactor.getInventoryName());

        this.fontRenderer.drawString(name, 70 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xC7C1A3);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int i = (int)(liquefactor.getPower() * 52 / liquefactor.getMaxPower());
        drawTexturedModalRect(guiLeft + 134, guiTop + 70 - i, 176, 52 - i, 16, i);

        int j = liquefactor.progress * 42 / liquefactor.processTime;
        drawTexturedModalRect(guiLeft + 42, guiTop + 17, 192, 0, j, 35);

        if(i > 0)
            drawTexturedModalRect(guiLeft + 138, guiTop + 4, 176, 52, 9, 12);

        FFUtils.drawLiquid(liquefactor.tank, guiLeft, guiTop, this.zLevel, 16, 52, 71, 116);
    }
}
