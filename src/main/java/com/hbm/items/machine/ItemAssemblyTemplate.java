package com.hbm.items.machine;

import com.github.bsideup.jabel.Desugar;
import com.hbm.interfaces.IHasCustomModel;
import com.hbm.inventory.AssemblerRecipes;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NbtComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.gui.GUIAssemfac;
import com.hbm.inventory.gui.GUIMachineAssembler;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemAssemblyTemplate extends Item implements IHasCustomModel {

    public static final ModelResourceLocation location = new ModelResourceLocation(RefStrings.MODID + ":assembly_template", "inventory");

    public ItemAssemblyTemplate(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(MainRegistry.templateTab);

        ModItems.ALL_ITEMS.add(this);
    }

    public static ItemStack getTemplate(int id) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("type", id);
        ItemStack stack = new ItemStack(ModItems.assembly_template, 1, 0);
        stack.setTagCompound(tag);
        return stack;
    }

    public static int getProcessTime(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemAssemblyTemplate)) return 100;
        int i = getTagWithRecipeNumber(stack).getInteger("type");
        if (i < 0 || i >= AssemblerRecipes.recipeList.size()) return 100;
        ComparableStack out = AssemblerRecipes.recipeList.get(i);
        Integer time = AssemblerRecipes.time.get(out);
        return time != null ? time : 100;
    }

    public static int getRecipeIndex(ItemStack stack) {
        return getTagWithRecipeNumber(stack).getInteger("type");
    }

    public static NBTTagCompound getTagWithRecipeNumber(@Nonnull ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
            stack.getTagCompound().setInteger("type", 0);
        }
        return stack.getTagCompound();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        String s = (I18n.format(this.getTranslationKey() + ".name")).trim();
        int damage = getTagWithRecipeNumber(stack).getInteger("type");
        ItemStack out = damage < AssemblerRecipes.recipeList.size() ? AssemblerRecipes.recipeList.get(damage).toStack() : ItemStack.EMPTY;
        String s1 = (I18n.format((out != ItemStack.EMPTY ? out.getTranslationKey() : "") + ".name")).trim();
        return s + " " + s1;
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list) {
        if (tab == this.getCreativeTab() || tab == CreativeTabs.SEARCH) {
            int count = AssemblerRecipes.recipeList.size();

            for (int i = 0; i < count; i++) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("type", i);
                ItemStack stack = new ItemStack(this, 1, 0);
                stack.setTagCompound(tag);
                list.add(stack);
            }
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
        if (!(stack.getItem() instanceof ItemAssemblyTemplate)) return;

        list.add("§6" + I18nUtil.resolveKey("info.templatefolder"));
        list.add("");

        Map<ComparableStack, Integer> availableCounts = null;
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GUIMachineAssembler assemblerGui && assemblerGui.getSlotUnderMouse()!= null) {
            IItemHandler assemblerInventory = assemblerGui.getInventory();
            if (assemblerInventory != null) {
                availableCounts = new HashMap<>();
                for (int slot = 6; slot < 18; slot++) {
                    countItem(availableCounts, assemblerInventory, slot);
                }
            }
        } else if (screen instanceof GUIAssemfac assemfacGui && assemfacGui.getSlotUnderMouse() != null) {
            int inventoryIndex = assemfacGui.getSlotUnderMouse().getSlotIndex();
            if (inventoryIndex >= 17 && (inventoryIndex - 17) % 14 == 0 && inventoryIndex < assemfacGui.getInventory().getSlots()) {
                int unitIndex = (inventoryIndex - 17) / 14;
                IItemHandler assemblerInventory = assemfacGui.getInventory();
                if (assemblerInventory != null) {
                    availableCounts = new HashMap<>();
                    int startSlot = 5 + unitIndex * 14;
                    int endSlot = 16 + unitIndex * 14;
                    for (int slot = startSlot; slot <= endSlot; slot++) {
                        countItem(availableCounts, assemblerInventory, slot);
                    }
                }
            }
        }

        int i = getTagWithRecipeNumber(stack).getInteger("type");

        if (i < 0 || i >= AssemblerRecipes.recipeList.size()) {
            list.add("I AM ERROR");
            return;
        }

        ComparableStack out = AssemblerRecipes.recipeList.get(i);

        if (out == null) {
            list.add("I AM ERROR");
            return;
        }

        Object[] in = AssemblerRecipes.recipes.get(out);

        if (in == null) {
            list.add("I AM ERROR");
            return;
        }

        ItemStack output = out.toStack();

        list.add("§l" + I18nUtil.resolveKey("info.template_out"));
        list.add(" §a" + output.getCount() + "x " + output.getDisplayName());
        list.add("§l" + I18nUtil.resolveKey("info.template_in_p"));

        for (Object o : in) {
            String prefix = "§c";
            if (o instanceof AStack ingredient) {
                if (availableCounts != null) {
                    CheckResult result = checkAndConsume(ingredient, new HashMap<>(availableCounts));
                    prefix = result.color();
                }
                if (ingredient instanceof ComparableStack input) {
                    list.add(" " + prefix + input.toStack().getCount() + "x " + input.toStack().getDisplayName());
                } else if (ingredient instanceof OreDictStack input) {
                    NonNullList<ItemStack> ores = OreDictionary.getOres(input.name);
                    if (!ores.isEmpty()) {
                        ItemStack inStack = ores.get((int) (Math.abs(System.currentTimeMillis() / 1000) % ores.size()));
                        list.add(" " + prefix + input.count() + "x " + inStack.getDisplayName());
                    } else {
                        list.add(prefix + "I AM ERROR - No OrdDict match found for " + input.name);
                    }
                }
            }
        }

        list.add("§l" + I18nUtil.resolveKey("info.template_time"));
        list.add(" §3" + Math.floor((float) (getProcessTime(stack)) / 20 * 100) / 100 + " " + I18nUtil.resolveKey("info.template_seconds"));
    }

    public static void countItem(Map<ComparableStack, Integer> availableCounts, IItemHandler inventory, int slot) {
        ItemStack stackInSlot = inventory.getStackInSlot(slot);
        if (!stackInSlot.isEmpty()) {
            ItemStack keyStack = stackInSlot.copy();
            keyStack.setCount(1);
            ComparableStack key = stackInSlot.hasTagCompound() ? new NbtComparableStack(keyStack) : new ComparableStack(keyStack);
            availableCounts.merge(key, stackInSlot.getCount(), Integer::sum);
        }
    }

    @SideOnly(Side.CLIENT)
    public static CheckResult checkAndConsume(AStack ingredient, Map<ComparableStack, Integer> availableCounts) {
        int required = ingredient.count();
        int totalAvailable = 0;
        List<ComparableStack> matchingKeys = new ArrayList<>();
        for (Map.Entry<ComparableStack, Integer> entry : availableCounts.entrySet()) {
            if (entry.getValue() > 0 && ingredient.matchesRecipe(entry.getKey().toStack(), true)) {
                totalAvailable += entry.getValue();
                matchingKeys.add(entry.getKey());
            }
        }
        String color;
        if (totalAvailable >= required) {
            color = "§a"; // Green: sufficient
        } else if (totalAvailable > 0) {
            color = "§6"; // Orange: present but insufficient
        } else {
            color = "§c"; // Red: not present
        }
        if (totalAvailable > 0) {
            int toConsume = Math.min(totalAvailable, required);
            for (ComparableStack key : matchingKeys) {
                if (toConsume <= 0) break;
                int countInStack = availableCounts.get(key);
                int consumedFromThisStack = Math.min(toConsume, countInStack);
                availableCounts.put(key, countInStack - consumedFromThisStack);
                toConsume -= consumedFromThisStack;
            }
        }
        return new CheckResult(color, totalAvailable);
    }

    @Override
    public ModelResourceLocation getResourceLocation() {
        return location;
    }

    @Desugar
    public record CheckResult(String color, int available) {
    }
}