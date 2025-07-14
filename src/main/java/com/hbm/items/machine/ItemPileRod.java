package com.hbm.items.machine;

import com.hbm.items.ItemBakedBase;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemPileRod extends ItemBakedBase {

	public ItemPileRod(String s){
		super(s);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		String[] defaultLocs = I18nUtil.resolveKey("desc.item.pileRod").split("\\$");

		for(String loc : defaultLocs) {
			tooltip.add(loc);
		}

		String[] descLocs = I18nUtil.resolveKey(this.getTranslationKey() + ".desc").split("\\$");

		for(String loc : descLocs) {
			tooltip.add(loc);
		}
	}
}