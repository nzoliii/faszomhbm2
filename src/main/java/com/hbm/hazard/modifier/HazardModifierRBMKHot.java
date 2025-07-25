package com.hbm.hazard.modifier;

import com.hbm.items.machine.ItemRBMKRod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class HazardModifierRBMKHot extends HazardModifier {

	@Override
	public float modify(final ItemStack stack, final EntityLivingBase holder, float level) {
		
		level = 0;
		
		if(stack.getItem() instanceof ItemRBMKRod) {
			final double heat = ItemRBMKRod.getHullHeat(stack);
			final int fire = (int)Math.min(Math.ceil((heat - 100) / 10D), 60);
			level = fire;
		}
		
		return level;
	}

}
