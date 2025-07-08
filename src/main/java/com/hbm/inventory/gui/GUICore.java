package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCore;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GUICore extends GuiInfoContainer {

    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/dfc/gui_core.png");
    private final TileEntityCore core;

    public GUICore(InventoryPlayer invPlayer, TileEntityCore tedf) {
        super(new ContainerCore(invPlayer, tedf));
        core = tedf;

        this.xSize = 176;
        this.ySize = 204;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);

        core.tanks[0].renderTankInfo(this, mouseX, mouseY, guiLeft + 26, guiTop + 17, 16, 52);
        core.tanks[1].renderTankInfo(this, mouseX, mouseY, guiLeft + 134, guiTop + 17, 16, 52);

        String[] heat = new String[]{"Heat Saturation: " + core.heat + "%"};
        String[] field = new String[]{"Restriction Field: " + core.field + "%"};
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 97, 70, 4, mouseX, mouseY, heat);
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 53, guiTop + 101, 70, 4, mouseX, mouseY, field);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {

        String name = this.core.hasCustomInventoryName() ? this.core.getInventoryName() : I18n.format(this.core.getInventoryName()).trim();
        this.fontRenderer.drawString(name, this.xSize - 8 - this.fontRenderer.getStringWidth(name), this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int i = core.getHeatScaled(70);
        if (i > 70)
            i = 70;
        drawTexturedModalRect(guiLeft + 53, guiTop + 98, 0, 204, i, 4);

        int j = core.getFieldScaled(70);
        if (j > 70)
            j = 70;
        drawTexturedModalRect(guiLeft + 53, guiTop + 102, 0, 208, j, 4);

        if (core.hasCore())
            drawTexturedModalRect(guiLeft + 70, guiTop + 29, 220, 0, 36, 36);

        core.tanks[0].renderTank(guiLeft + 26, guiTop + 69, this.zLevel, 16, 52);
        core.tanks[1].renderTank(guiLeft + 134, guiTop + 69, this.zLevel, 16, 52);
    }
}