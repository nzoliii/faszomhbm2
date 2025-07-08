package com.hbm.items.special;

import com.hbm.inventory.WasteDrumRecipes;
import com.hbm.util.I18nUtil;
import com.hbm.util.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemDepletedFuel extends ItemNuclearWaste {

    private static final List<ItemDepletedFuel> INSTANCES = new ArrayList<>();

    public ItemDepletedFuel(String s) {
        super(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            INSTANCES.add(this);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
        if (this.isInCreativeTab(tab)) {
            for (int i = 0; i < 2; i++) {
                list.add(new ItemStack(this, 1, i));
            }
        }
    }



    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn){
        if (stack.getItemDamage() > 0)
            tooltip.add(TextFormatting.GOLD + I18nUtil.resolveKey("desc.item.wasteCooling"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerColorHandlers() {
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        IItemColor handler = new ColorHandler();

        for (ItemDepletedFuel item : INSTANCES) {
            itemColors.registerItemColorHandler(handler, item);
        }
    }

    public static void registerPoolRecepies(){
        for(ItemDepletedFuel waste : INSTANCES){
            WasteDrumRecipes.addRecipe(ItemStackUtil.comparableStackFrom(waste, 1, 1), new ItemStack(waste, 1, 0));
        }
    }

    private static class ColorHandler implements IItemColor {
        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            return stack.getMetadata() > 0 ? 0xFFBFA5 : 0xFFFFFF;
        }

    }
}
