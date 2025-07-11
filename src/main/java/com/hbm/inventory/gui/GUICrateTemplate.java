package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerCrateTemplate;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityCrateTemplate;
import com.hbm.tileentity.machine.storage.TileEntityCrateBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GUICrateTemplate extends GuiContainer {

    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_crate_template.png");
    private final TileEntityCrateTemplate diFurnace;

    public GUICrateTemplate(InventoryPlayer invPlayer, TileEntityCrateTemplate tedf) {
        super(new ContainerCrateTemplate(invPlayer, tedf));
        diFurnace = tedf;

        this.xSize = 176;
        this.ySize = 168;
    }

    public void initGui() {
        super.initGui();
        if (mc.player != null) {
            TileEntityCrateBase.openInventory(mc.player);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (mc.player != null) {
            TileEntityCrateBase.closeInventory(mc.player);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.diFurnace.hasCustomInventoryName() ? this.diFurnace.getInventoryName() : I18n.format(this.diFurnace.getInventoryName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }
}