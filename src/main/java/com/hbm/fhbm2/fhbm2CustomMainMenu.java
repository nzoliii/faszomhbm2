package com.hbm.fhbm2;

import com.hbm.lib.RefStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiModList;

import java.io.IOException;

public class fhbm2CustomMainMenu extends GuiMainMenu {

    private String[] textLines;

    private void initializeTextLines() {
        textLines = new String[] {
                "You are playing FaszomHBM 2 " + RefStrings.VERSION,
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

        this.buttonList.add(new fhbm2GUIButton(101, this.width / 2 - 100, yOffset, 200, 20, "Singleplayer"));
        this.buttonList.add(new fhbm2GUIButton(102, this.width / 2 - 100, yOffset + 24, 200, 20, "Multiplayer"));
        this.buttonList.add(new fhbm2GUIButton(103, this.width / 2 - 100, yOffset + 48, 98, 20, "Mods"));
        this.buttonList.add(new fhbm2GUIButton(104, this.width / 2 + 2, yOffset + 48, 98, 20, "Realms"));
        this.buttonList.add(new fhbm2GUIButtonLanguage(105, this.width / 2 - 124, j + 72 + 12));
        this.buttonList.add(new fhbm2GUIButton(106, this.width / 2 - 100, yOffset + 84, 98, 20, "Options..."));
        this.buttonList.add(new fhbm2GUIButton(107, this.width / 2 + 2, yOffset + 84, 98, 20, "Quit Game"));
        this.buttonList.add(new fhbm2GUIButton(108, this.width / 2 + 104, yOffset + 84, 20, 20, "SM"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("hbm", "textures/gui/title/background2.png"));
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
        if (button instanceof fhbm2GUIButton) { fhbm2GUIButton customButton = (fhbm2GUIButton) button; }

        if (button.id == 101) { mc.displayGuiScreen(new GuiWorldSelection(this)); }
        if (button.id == 102) { mc.displayGuiScreen(new GuiMultiplayer(this)); }
        if (button.id == 103) { mc.displayGuiScreen(new GuiModList(this)); }
        if (button.id == 104) { switchToRealms(); }
        if (button.id == 105) { mc.displayGuiScreen(new GuiLanguage(this, mc.gameSettings, mc.getLanguageManager())); }
        if (button.id == 106) { mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)); }
        if (button.id == 107) { mc.shutdown(); }
        if (button.id == 108) { fhbm2MenuStateManager.setCustomMenuEnabled(false); mc.displayGuiScreen(new GuiMainMenu()); } else { super.actionPerformed(button); }
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

//    Used to have extra links in the main menu for Wiki and Source Code,
//    then I realized that it wouldn't fit in higher GUI scales, and I was lazy to fix it.
//
//    This was used to open the links. I just comment it in case I need it again.
//
//    private void openLink(String url) {
//        try {
//            Desktop.getDesktop().browse(new URI(url));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}