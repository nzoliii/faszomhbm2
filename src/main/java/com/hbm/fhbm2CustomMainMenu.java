package com.hbm;

import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class fhbm2CustomMainMenu extends GuiMainMenu {

    private String[] textLines;

    private void initializeTextLines() {
        textLines = new String[] {
                "You are playing FaszomHBM 2 " + RefStrings.VERSION,
                "",
                "Copyright Mojang AB. Do not distribute!"
        };
    }

    public fhbm2CustomMainMenu() {
        initializeTextLines();
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int yOffset = this.height / 4 + 48;

        int j = this.height / 4 + 48;

        this.buttonList.add(new GuiButton(101, this.width / 2 - 100, yOffset, 200, 20, "Singleplayer"));
        this.buttonList.add(new GuiButton(102, this.width / 2 - 100, yOffset + 24, 200, 20, "Multiplayer"));
        this.buttonList.add(new GuiButton(103, this.width / 2 - 100, yOffset + 48, 98, 20, "Mods"));
        this.buttonList.add(new GuiButton(104, this.width / 2 + 2, yOffset + 48, 98, 20, "Realms"));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, j + 72 + 12));
        this.buttonList.add(new GuiButton(106, this.width / 2 - 100, yOffset + 84, 98, 20, "Options..."));
        this.buttonList.add(new GuiButton(107, this.width / 2 + 2, yOffset + 84, 98, 20, "Quit Game"));
        this.buttonList.add(new GuiButton(108, this.width / 2 + 104, yOffset + 84, 20, 20, "SM"));
        this.buttonList.add(new GuiButton(109, this.width / 2 - 100, yOffset + 120, 98, 20, "Wiki"));
        this.buttonList.add(new GuiButton(110 , this.width / 2 + 2, yOffset + 120, 98, 20, "Tutorial Videos"));
        this.buttonList.add(new GuiButton(111, this.width / 2 - 100, yOffset + 144, 200, 20, "Source Code"));
        this.buttonList.add(new GuiButton(112, this.width / 2 - 100, yOffset + 168, 200, 20, "Copper Pig"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("hbm", "textures/gui/title/background.png"));
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        Minecraft mc = Minecraft.getMinecraft();

        int baseWidth = 300;
        int baseHeight = 75;

        float scaleFactor = 0.9F;

        int textureWidth = (int) (baseWidth * scaleFactor);
        int textureHeight = (int) (baseHeight * scaleFactor);

        int xPosition = (this.width / 2) - (textureWidth / 2);
        int yPosition = 30;

        if (xPosition < 0) {xPosition = 0;}
        if (xPosition + textureWidth > this.width) {xPosition = this.width - textureWidth;}

        if (yPosition < 0) {yPosition = 0;}
        if (yPosition + textureHeight > this.height) {yPosition = this.height - textureHeight;}

        mc.getTextureManager().bindTexture(new ResourceLocation("hbm", "textures/branding/fhbm2_title.png"));

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        this.buttonList.forEach(button -> button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks));
        drawCustomText();
    }

    private void drawCustomText() {
        int lineHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        int yOffset = this.height - 10;

        for (int i = textLines.length - 1; i >= 0; i--) {
            String text = textLines[i];
            int textWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
            int x = this.width - textWidth - 2;
            drawString(Minecraft.getMinecraft().fontRenderer, text, x, yOffset, 0xFFFFFF);

            yOffset -= lineHeight;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 101) { Minecraft.getMinecraft().displayGuiScreen(new GuiWorldSelection(this)); }
        if (button.id == 102) { Minecraft.getMinecraft().displayGuiScreen(new GuiMultiplayer(this)); }
        if (button.id == 103) { Minecraft.getMinecraft().displayGuiScreen(new GuiModList(this)); }
        if (button.id == 104) { switchToRealms(); }
        if (button.id == 106) { Minecraft.getMinecraft().displayGuiScreen(new GuiOptions(this, Minecraft.getMinecraft().gameSettings)); }
        if (button.id == 107) { Minecraft.getMinecraft().shutdown(); }
        if (button.id == 108) { fhbm2MenuStateManager.setCustomMenuEnabled(false); this.mc.displayGuiScreen(new GuiMainMenu()); } else { super.actionPerformed(button); }
        if (button.id == 109) { openLink("https://nucleartech.wiki/wiki/Main_Page"); }
        if (button.id == 110) { openLink("https://www.youtube.com/@SavageVegeta"); }
        if (button.id == 111) { openLink("https://github.com/nzoliii/faszomhbm2"); }
        if (button.id == 112) { openLink("https://media.tenor.com/if5dKoJC95IAAAAe/%D0%BA%D0%B0%D0%B1%D0%B0%D0%BD-boar.png"); }
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        this.width = width;
        this.height = height;
        this.initGui();
    }

    private void switchToRealms() {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}