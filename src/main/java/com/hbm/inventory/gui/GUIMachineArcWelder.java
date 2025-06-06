package com.hbm.inventory.gui;

import com.hbm.forgefluid.FFUtils;
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerMachineArcWelder;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineArcWelder extends GuiInfoContainer {

	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_arc_welder.png");
	private TileEntityMachineArcWelder welder;

	public GUIMachineArcWelder(InventoryPlayer playerInv, TileEntityMachineArcWelder tile) {
		super(new ContainerMachineArcWelder(playerInv, tile));
		
		this.welder = tile;
		this.xSize = 176;
		this.ySize = 204;
	}
	
	@Override
	public void drawScreen(int x, int y, float interp) {
		super.drawScreen(x, y, interp);

		FFUtils.renderTankInfo(this, x, y, guiLeft + 17, guiTop + 62, 52, 16, welder.tank);
		this.drawElectricityInfo(this, x, y, guiLeft + 152, guiTop + 17, 16, 52, welder.getPower(), welder.getMaxPower());
		
//		this.drawCustomInfoStat(x, y, guiLeft + 78, guiTop + 67, 8, 8, guiLeft + 78, guiTop + 67, this.getUpgradeInfo(welder));
		super.renderHoveredToolTip(x, y);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		String name = this.welder.hasCustomInventoryName() ? this.welder.getInventoryName() : I18n.format(this.welder.getInventoryName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2 - 18, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float interp, int x, int y) {
		super.drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int p = (int) (welder.power * 52 / Math.max(welder.maxPower, 1));
		drawTexturedModalRect(guiLeft + 152, guiTop + 70 - p, 176, 52 - p, 16, p);
		
		int i = welder.progress * 33 / Math.max(welder.processTime, 1);
		drawTexturedModalRect(guiLeft + 72, guiTop + 37, 192, 0, i, 14);
		
		if(welder.power >= welder.consumption) {
			drawTexturedModalRect(guiLeft + 156, guiTop + 4, 176, 52, 9, 12);
		}

//		this.drawInfoPanel(guiLeft + 78, guiTop + 67, 8, 8, 8);
		FFUtils.drawLiquid(welder.tank, guiLeft, guiTop, this.zLevel, 52, 16, 17, 107);
	}
}
