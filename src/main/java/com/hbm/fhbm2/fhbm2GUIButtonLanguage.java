package com.hbm.fhbm2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class fhbm2GUIButtonLanguage extends GuiButton {

    private static final ResourceLocation CUSTOM_BUTTON_TEXTURES = new ResourceLocation("hbm", "textures/gui/widgets.png");

    public fhbm2GUIButtonLanguage(int buttonID, int xPos, int yPos)
    {
        super(buttonID, xPos, yPos, 20, 20, "");
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(CUSTOM_BUTTON_TEXTURES);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = 106;

            if (flag)
            {
                i += this.height;
            }

            this.drawTexturedModalRect(this.x, this.y, 0, i, this.width, this.height);
        }
    }
}