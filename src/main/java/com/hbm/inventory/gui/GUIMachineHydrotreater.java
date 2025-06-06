package com.hbm.inventory.gui;

import com.hbm.forgefluid.FFUtils;
import com.hbm.inventory.container.ContainerMachineHydrotreater;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GUIMachineHydrotreater extends GuiInfoContainer {

    private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_hydrotreater.png");
    private TileEntityMachineHydrotreater hydrotreater;

    public GUIMachineHydrotreater(InventoryPlayer invPlayer, TileEntityMachineHydrotreater tedf) {
        super(new ContainerMachineHydrotreater(invPlayer, tedf));
        hydrotreater = tedf;

        this.xSize = 176;
        this.ySize = 238;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float f) {
        super.drawScreen(mouseX, mouseY, f);
        super.renderHoveredToolTip(mouseX, mouseY);
        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 35, guiTop + 69 - 52, 16, 52, hydrotreater.tanks[0]);
        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 53, guiTop + 69 - 52, 16, 52, hydrotreater.tanks[1]);
        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 125, guiTop + 69 - 52, 16, 52, hydrotreater.tanks[2]);
        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 143, guiTop + 69 - 52, 16, 52, hydrotreater.tanks[3]);
        this.drawElectricityInfo(this, mouseX, mouseY, guiLeft + 17, guiTop + 69 - 52, 16, 52, hydrotreater.power, hydrotreater.maxPower);

        if(this.mc.player.inventory.getItemStack().isEmpty() && this.isMouseOverSlot(this.inventorySlots.getSlot(10), mouseX, mouseY) && !this.inventorySlots.getSlot(10).getHasStack()) {
            List<Object[]> lines = new ArrayList<>();
            ItemStack converter = new ItemStack(ModItems.catalytic_converter);
            lines.add(new Object[] {converter});
            lines.add(new Object[] {converter.getDisplayName()});
            this.drawStackText(lines, mouseX, mouseY, this.fontRenderer, 0);
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int j) {
        String name = this.hydrotreater.hasCustomInventoryName() ? this.hydrotreater.getInventoryName() : I18n.format(this.hydrotreater.getInventoryName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 5, 0xffffff);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int p_146976_3_) {
        super.drawDefaultBackground();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int j = (int) (hydrotreater.power * 54 / TileEntityMachineHydrotreater.maxPower);
        drawTexturedModalRect(guiLeft + 17, guiTop + 70 - j, 176, 52 - j, 16, j);

        FFUtils.drawLiquid(hydrotreater.tanks[0], guiLeft, guiTop, this.zLevel, 16, 52, 35, 98);
        FFUtils.drawLiquid(hydrotreater.tanks[1], guiLeft, guiTop, this.zLevel, 16, 52, 53, 98);
        FFUtils.drawLiquid(hydrotreater.tanks[2], guiLeft, guiTop, this.zLevel, 16, 52, 125, 98);
        FFUtils.drawLiquid(hydrotreater.tanks[3], guiLeft, guiTop, this.zLevel, 16, 52, 143, 98);
    }
}
