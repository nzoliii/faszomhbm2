package com.hbm.items.machine;

import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemStamp extends Item {

	public ItemStamp(String s, int dura) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setMaxDamage(dura);
		this.setCreativeTab(MainRegistry.controlTab);
		this.setMaxStackSize(1);
		
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(this == ModItems.stamp_iron_circuit ||
				this == ModItems.stamp_iron_plate ||
				this == ModItems.stamp_iron_wire ||
				this == ModItems.stamp_obsidian_circuit ||
				this == ModItems.stamp_obsidian_plate ||
				this == ModItems.stamp_obsidian_wire ||
				this == ModItems.stamp_desh_circuit ||
				this == ModItems.stamp_desh_plate ||
				this == ModItems.stamp_desh_wire ||
				this == ModItems.stamp_steel_circuit ||
				this == ModItems.stamp_steel_plate ||
				this == ModItems.stamp_steel_wire ||
				this == ModItems.stamp_titanium_circuit ||
				this == ModItems.stamp_titanium_plate ||
				this == ModItems.stamp_titanium_wire ||
				this == ModItems.stamp_stone_circuit ||
				this == ModItems.stamp_stone_plate ||
				this == ModItems.stamp_stone_wire)
			tooltip.add("§e" + I18nUtil.resolveKey("info.templatefolder", I18nUtil.resolveKey("item.template_folder.name")));
		if(stack.getMaxDamage() > 0 && stack.getItemDamage() == 0) tooltip.add("Durability: "+ stack.getMaxDamage() + " / " + stack.getMaxDamage());
	}
}
