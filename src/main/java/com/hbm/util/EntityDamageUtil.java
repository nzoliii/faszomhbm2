package com.hbm.util;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

public class EntityDamageUtil {

	public static boolean attackEntityFromIgnoreIFrame(Entity victim, DamageSource src, float damage) {

		if(!victim.attackEntityFrom(src, damage)) {
			float dmg = damage + ((EntityLivingBase)victim).lastDamage;
			return victim.attackEntityFrom(src, dmg);
		} else {
			return true;
		}
	}

	public static float getLastDamage(Entity victim) {
		try {
			return ((EntityLivingBase)victim).lastDamage;
		} catch(Exception x) {
			return 0F;
		}
	}
	
	public static boolean wasAttackedByV1(DamageSource source) {

		if(source instanceof EntityDamageSource) {
			Entity attacker = ((EntityDamageSource) source).getImmediateSource();
			
			if(attacker instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) attacker;
				ItemStack chestplate = player.inventory.armorInventory.get(2);
				
				if(chestplate != null && ArmorModHandler.hasMods(chestplate)) {
					ItemStack[] mods = ArmorModHandler.pryMods(chestplate);
					
					if(mods[ArmorModHandler.extra] != null && mods[ArmorModHandler.extra].getItem() == ModItems.v1) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
}
