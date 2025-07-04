package com.hbm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class fhbm2GUIButton extends GuiButton {

    private static final ResourceLocation CUSTOM_BUTTON_TEXTURES = new ResourceLocation("hbm", "textures/gui/widgets.png");

    public fhbm2GUIButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    public fhbm2GUIButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, 200, 20, buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(CUSTOM_BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y &&
                    mouseX < this.x + this.width && mouseY < this.y + this.height;

            int hoverState = this.getHoverState(this.hovered);
            int v = 46 + hoverState * 20;

            drawTexturedModalRect(this.x, this.y, 0, v, this.width / 2, this.height);
            drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, v, this.width / 2, this.height);

            this.mouseDragged(mc, mouseX, mouseY);

            int color = packedFGColour;
            if (color == 0) { color = this.enabled ? (this.hovered ? 0xFFFFA0 : 0xE0E0E0) : 0xA0A0A0; }

            drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, color);
        }
    }
}
