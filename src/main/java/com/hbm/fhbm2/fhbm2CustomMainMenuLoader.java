package com.hbm.fhbm2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class fhbm2CustomMainMenuLoader {

    private boolean customMenuDisplayed = false;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu
                && fhbm2CustomMainMenuStateManager.isCustomMenuEnabled()
                && !customMenuDisplayed) {

            Minecraft.getMinecraft().displayGuiScreen(new fhbm2CustomMainMenu());
            customMenuDisplayed = true;
        }

        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu)) {
            customMenuDisplayed = false;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu && !fhbm2CustomMainMenuStateManager.isCustomMenuEnabled()) {
            int yOffset = event.getGui().height / 4 + 48;
            event.getButtonList().add(new GuiButton(108, event.getGui().width / 2 + 104, yOffset + 84, 20, 20, "SM"));
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGuiButtonPress(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (event.getButton().id == 108 && event.getGui() instanceof GuiMainMenu) {
            fhbm2CustomMainMenuStateManager.setCustomMenuEnabled(true);
            Minecraft.getMinecraft().displayGuiScreen(new fhbm2CustomMainMenu());
        }
    }
}