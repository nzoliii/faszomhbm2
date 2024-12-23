package com.hbm.items.food;

import java.util.List;
import java.util.UUID;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ModItems;
import com.hbm.interfaces.IItemHazard;
import com.hbm.modules.ItemHazardModule;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.item.ItemFood;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

public class ItemFHBM2Consumables extends ItemFood implements IItemHazard {

	ItemHazardModule module;

	private boolean alwaysEdible;


	public ItemFHBM2Consumables(int amount, float saturation, boolean isWolfFood, String s){
		super(amount, saturation, isWolfFood);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		this.module = new ItemHazardModule();

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public ItemHazardModule getModule() {
		return this.module;
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
		if(entity instanceof EntityLivingBase)
			this.module.applyEffects((EntityLivingBase) entity, stack.getCount(), itemSlot, isSelected, ((EntityLivingBase)entity).getHeldItem(EnumHand.MAIN_HAND) == stack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn){
		if(this == ModItems.fhbm2_iceberg_arasaka) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_iceberg_black) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_iceberg_crazy_mix) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_iceberg_dragonfire) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
			list.add("Tuzvarazslo shouldn't use this.");
		}
		if(this == ModItems.fhbm2_iceberg_emerald) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_iceberg_sour_berries) {
			list.add("Iceberg nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_copper_pig_fragment) {
			list.add("§oI must become one with the Copper Pig.");
			list.add("§oI must consume this fragment.");
			list.add("A fragment of the most gracious the most merciful Copper Pig, кабан.");
			list.add("If the Copper Pig has a million fans, then I am one of them.");
			list.add("If the Copper Pig has ten fans, then I am one of them.");
			list.add("If the Copper Pig has only one fan then that is me.");
			list.add("If the Copper Pig has no fans, then that means I am no longer on earth.");
			list.add("If the world is against the Copper Pig, then I am against the world. ");
		}
		if(this == ModItems.fhbm2_mini_pablo) {
			list.add("Mini Pablo nicotine pouches.");
			list.add("Tastes awful.");
			list.add("Smells awful.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_zyn) {
			list.add("Zyn nicotine pouches.");
			list.add("Takes the edge off.");
		}
		if(this == ModItems.fhbm2_som) {
			list.add("God knows when it was made.");
			list.add("Tastes awful, yet we still drink it.");
		}
		if(this == ModItems.fhbm2_abalt_salonna) {
			list.add("The mighty Abált Szalonna.");
			list.add("Best food ever.");
		}
		if(this == ModItems.fhbm2_bucket_abale_old) {
			list.add("Tasty Abalé.");
			list.add("Why would anyone drink this?");
		}

		this.module.addInformation(stack, list, flagIn);
		super.addInformation(stack, world, list, flagIn);
	}

	@Override
	public boolean onEntityItemUpdate(EntityItem item){
		super.onEntityItemUpdate(item);
		return super.onEntityItemUpdate(item);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {

		if(stack.getItem() == ModItems.fhbm2_iceberg_arasaka){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_iceberg_black){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_iceberg_crazy_mix){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_iceberg_dragonfire) {
			UUID playerUUID = player.getUniqueID();
			UUID targetUUID = UUID.fromString("5af3c6bb-6b31-4c26-8766-a229572b1d2a");
			if (playerUUID.equals(targetUUID)) {
				worldIn.spawnEntity(EntityNukeExplosionMK5.statFac(worldIn, (int)(BombConfig.gadgetRadius * 0.5), player.posX, player.posY, player.posZ));
				EntityNukeTorex.statFac(worldIn, player.posX, player.posY, player.posZ, (int)(BombConfig.gadgetRadius * 0.5));
			}

			else {
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
			}
		}

		if(stack.getItem() == ModItems.fhbm2_iceberg_emerald){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_iceberg_sour_berries){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_copper_pig_fragment){
			player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 2147483647, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 2147483647, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 2147483647, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 2147483647, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 2147483647, 255));
			player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2147483647, 3));
			ContaminationUtil.contaminate(player, HazardType.DIGAMMA, ContaminationType.DIGAMMA, 5F);
		}

		if(stack.getItem() == ModItems.fhbm2_mini_pablo){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_zyn){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_som){
			player.addPotionEffect(new PotionEffect(MobEffects.POISON, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_abalt_salonna){
			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 15 * 60, 4));
		}

		if(stack.getItem() == ModItems.fhbm2_bucket_abale_old){
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 15 * 60, 4));
		}
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!VersatileConfig.hasPotionSickness(playerIn))
			playerIn.setActiveHand(handIn);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	//public ItemFood setAlwaysEdible() {
	//	this.alwaysEdible = true;
	//	return this;
	//}
}