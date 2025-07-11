package com.hbm.inventory.gui;

import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GuiInfoContainer extends GuiContainer {

  ResourceLocation guiUtil =
      new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_utility.png");

  public GuiInfoContainer(Container inventorySlotsIn) {
    super(inventorySlotsIn);
  }

  public void drawFluidInfo(String[] text, int x, int y) {
    this.drawHoveringText(Arrays.asList(text), x, y);
  }

  public void drawFluidInfo(List<String> text, int x, int y) {
    this.drawHoveringText(text, x, y);
  }

  //the mojang employee who made this private on the super can explode
  // mlbv: bro you can use AccessTransformer to make it public

  public void drawElectricityInfo(
      GuiInfoContainer gui,
      int mouseX,
      int mouseY,
      int x,
      int y,
      int width,
      int height,
      long power,
      long maxPower) {
    if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
      gui.drawFluidInfo(
          new String[] {
            Library.getShortNumber(power) + "/" + Library.getShortNumber(maxPower) + "HE"
          },
          mouseX,
          mouseY);
  }

  public void drawCustomInfo(
      int mouseX, int mouseY, int x, int y, int width, int height, String[] text) {
    if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
      this.drawHoveringText(Arrays.asList(text), mouseX, mouseY);
  }

  public void drawCustomInfoStat(
      int mouseX,
      int mouseY,
      int x,
      int y,
      int width,
      int height,
      int tPosX,
      int tPosY,
      String[] text) {
    drawCustomInfoStat(mouseX, mouseY, x, y, width, height, tPosX, tPosY, Arrays.asList(text));
  }

  public void drawCustomInfoStat(
      int mouseX,
      int mouseY,
      int x,
      int y,
      int width,
      int height,
      int tPosX,
      int tPosY,
      List<String> lines) {
    if (x <= mouseX && x + width > mouseX && y < mouseY && y + height >= mouseY)
      this.drawHoveringText(lines, tPosX, tPosY);
  }

  /**
   * Automatically grabs upgrade info out of the tile entity if it's a IUpgradeInfoProvider and
   * crams the available info into a list for display. Automation, yeah!
   */
  public List<String> getUpgradeInfo(TileEntity tile) {
    List<String> lines = new ArrayList<>();

    if (tile instanceof IUpgradeInfoProvider provider) {

      lines.add(I18nUtil.resolveKey("upgrade.gui.title"));

      for (UpgradeType type : UpgradeType.values()) {
        if (provider.canProvideInfo(type, 0, false)) {
          int maxLevel = provider.getValidUpgrades().get(type);
          switch (type) {
            case SPEED:
              lines.add(I18nUtil.resolveKey("upgrade.gui.speed", maxLevel));
              break;
            case POWER:
              lines.add(I18nUtil.resolveKey("upgrade.gui.power", maxLevel));
              break;
            case EFFECT:
              lines.add(I18nUtil.resolveKey("upgrade.gui.effectiveness", maxLevel));
              break;
            case AFTERBURN:
              lines.add(I18nUtil.resolveKey("upgrade.gui.afterburner", maxLevel));
              break;
            case OVERDRIVE:
              lines.add(I18nUtil.resolveKey("upgrade.gui.overdrive", maxLevel));
              break;
            default:
              break;
          }
        }
      }
    }

    return lines;
  }

  public void drawInfoPanel(int x, int y, int width, int height, int type) {

    Minecraft.getMinecraft().getTextureManager().bindTexture(guiUtil);

    switch (type) {
      case 0:
        // Small blue I
        drawTexturedModalRect(x, y, 0, 0, 8, 8);
        break;
      case 1:
        // Small green I
        drawTexturedModalRect(x, y, 0, 8, 8, 8);
        break;
      case 2:
        // Large blue I
        drawTexturedModalRect(x, y, 8, 0, 16, 16);
        break;
      case 3:
        // Large green I
        drawTexturedModalRect(x, y, 24, 0, 16, 16);
        break;
      case 4:
        // Small red !
        drawTexturedModalRect(x, y, 0, 16, 8, 8);
        break;
      case 5:
        // Small yellow !
        drawTexturedModalRect(x, y, 0, 24, 8, 8);
        break;
      case 6:
        // Large red !
        drawTexturedModalRect(x, y, 8, 16, 16, 16);
        break;
      case 7:
        // Large yellow !
        drawTexturedModalRect(x, y, 24, 16, 16, 16);
        break;
      case 8:
        // Small blue *
        drawTexturedModalRect(x, y, 0, 32, 8, 8);
        break;
      case 9:
        // Small grey *
        drawTexturedModalRect(x, y, 0, 40, 8, 8);
        break;
      case 10:
        // Large blue *
        drawTexturedModalRect(x, y, 8, 32, 16, 16);
        break;
      case 11:
        // Large grey *
        drawTexturedModalRect(x, y, 24, 32, 16, 16);
        break;
    }
  }

  protected void drawStackText(
      List<Object[]> lines, int x, int y, FontRenderer font, int highLightIndex) {
    if (lines.isEmpty()) return;

    GlStateManager.color(1F, 1F, 1F, 1F);
    GlStateManager.disableRescaleNormal();
    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableLighting();
    GlStateManager.disableDepth();

    int tooltipHeight = 0;
    int tooltipWidth = 0;

    for (Object[] line : lines) {
      int lineWidth = 0;
      boolean hasStack = false;

      for (Object o : line) {

        if (o instanceof String) {
          lineWidth += font.getStringWidth((String) o);
        } else {
          lineWidth += 18;
          hasStack = true;
        }
      }

      if (hasStack) {
        tooltipHeight += 18;
      } else {
        tooltipHeight += 10;
      }

      if (lineWidth > tooltipWidth) {
        tooltipWidth = lineWidth;
      }
    }

    int minX = x + 12;
    int minY = y - 12;

    if (minX + tooltipWidth > this.width) {
      minX -= 28 + tooltipWidth;
    }

    if (minY + tooltipHeight + 6 > this.height) {
      minY = this.height - tooltipHeight - 6;
    }

    this.zLevel = 400.0F;
    itemRender.zLevel = 400.0F;

    int colorBg = 0xF0100010;
    this.drawGradientRect(minX - 3, minY - 4, minX + tooltipWidth + 3, minY - 3, colorBg, colorBg);
    this.drawGradientRect(
        minX - 3,
        minY + tooltipHeight + 3,
        minX + tooltipWidth + 3,
        minY + tooltipHeight + 4,
        colorBg,
        colorBg);
    this.drawGradientRect(
        minX - 3, minY - 3, minX + tooltipWidth + 3, minY + tooltipHeight + 3, colorBg, colorBg);
    this.drawGradientRect(minX - 4, minY - 3, minX - 3, minY + tooltipHeight + 3, colorBg, colorBg);
    this.drawGradientRect(
        minX + tooltipWidth + 3,
        minY - 3,
        minX + tooltipWidth + 4,
        minY + tooltipHeight + 3,
        colorBg,
        colorBg);

    int color0 = 0x505000FF;
    int color1 = (color0 & 0xFEFEFE) >> 1 | color0 & 0xFF000000;

    this.drawGradientRect(
        minX - 3, minY - 3 + 1, minX - 3 + 1, minY + tooltipHeight + 3 - 1, color0, color1);
    this.drawGradientRect(
        minX + tooltipWidth + 2,
        minY - 3 + 1,
        minX + tooltipWidth + 3,
        minY + tooltipHeight + 3 - 1,
        color0,
        color1);
    this.drawGradientRect(
        minX - 3, minY - 3, minX + tooltipWidth + 3, minY - 3 + 1, color0, color0);
    this.drawGradientRect(
        minX - 3,
        minY + tooltipHeight + 2,
        minX + tooltipWidth + 3,
        minY + tooltipHeight + 3,
        color1,
        color1);

    int totalLen = 0;
    for (int index = 0; index < lines.size(); index++) {

      Object[] line = lines.get(index);
      int indent = 0;
      boolean hasStack = false;

      for (Object o : line) {
        if (!(o instanceof String)) {
          hasStack = true;
          break;
        }
      }

      for (int i = 0; i < line.length; i++) {
        Object o = line[i];
        if (o instanceof String) {
          font.drawStringWithShadow((String) o, minX + indent, minY + (hasStack ? 4 : 0), -1);
          indent += font.getStringWidth((String) o) + 2;
        } else {
          ItemStack stack = (ItemStack) o;
          GlStateManager.color(1F, 1F, 1F);

          if (totalLen + i == highLightIndex) {
            this.drawGradientRect(
                minX + indent - 1, minY - 1, minX + indent + 17, minY + 17, 0xffff0000, 0xffff0000);
            this.drawGradientRect(
                minX + indent, minY, minX + indent + 16, minY + 16, 0xff808080, 0xff808080);
          }
          GlStateManager.enableDepth();
          RenderHelper.enableGUIStandardItemLighting();
          itemRender.renderItemAndEffectIntoGUI(stack, minX + indent, minY);
          itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack, minX + indent, minY, "");
          RenderHelper.disableStandardItemLighting();
          GlStateManager.disableDepth();
          indent += 18;
        }
      }

      if (index == 0) {
        minY += 2;
      }
      totalLen += line.length;
      minY += hasStack ? 18 : 10;
    }

    this.zLevel = 0.0F;
    itemRender.zLevel = 0.0F;
    GlStateManager.enableLighting();
    GlStateManager.enableDepth();
    RenderHelper.enableStandardItemLighting();
    GlStateManager.enableRescaleNormal();
  }

  protected boolean checkClick(int x, int y, int left, int top, int sizeX, int sizeY) {
    return guiLeft + left <= x
        && guiLeft + left + sizeX > x
        && guiTop + top < y
        && guiTop + top + sizeY >= y;
  }

  protected void playPressSound() {
    mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
  }
}
