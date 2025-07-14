package com.hbm.items.weapon;

import com.hbm.items.special.ItemCustomLore;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemMissileStandard extends ItemCustomLore {

	public final MissileFormFactor formFactor;
	public final MissileTier tier;
	public final MissileFuel fuel;

	public int fuelCap;
	public boolean launchable = true;


	public ItemMissileStandard(String s, MissileFormFactor form, MissileTier tier) {
		this(s, form, tier, form.defaultFuel);
	}

	public ItemMissileStandard(String s, MissileFormFactor form, MissileTier tier, MissileFuel fuel) {
		super(s);
		this.formFactor = form;
		this.tier = tier;
		this.fuel = fuel;
		this.setFuelCap(this.fuel.defaultCap);
	}

	public ItemMissileStandard notLaunchable() {
		this.launchable = false;
		return this;
	}

	public ItemMissileStandard setFuelCap(int fuelCap) {
		this.fuelCap = fuelCap;
		return this;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		list.add(TextFormatting.ITALIC + this.tier.display);

		if (!this.launchable) {
			list.add(TextFormatting.RED + "Not launchable!");
		} else {
			list.add("Fuel: " + this.fuel.display);
			if (this.fuelCap > 0) {
				list.add("Fuel capacity: " + this.fuelCap + "mB");
			}
			super.addInformation(stack, worldIn, list, flagIn);
		}
	}

	public enum MissileFormFactor {
		ABM(MissileFuel.SOLID),
		MICRO(MissileFuel.SOLID),
		V2(MissileFuel.ETHANOL_PEROXIDE),
		STRONG(MissileFuel.KEROSENE_PEROXIDE),
		HUGE(MissileFuel.KEROSENE_LOXY),
		ATLAS(MissileFuel.JETFUEL_LOXY),
		OTHER(MissileFuel.KEROSENE_PEROXIDE);

		private final MissileFuel defaultFuel;

		MissileFormFactor(MissileFuel defaultFuel) {
			this.defaultFuel = defaultFuel;
		}
	}

	public enum MissileTier {
		TIER0("Tier 0"),
		TIER1("Tier 1"),
		TIER2("Tier 2"),
		TIER3("Tier 3"),
		TIER4("Tier 4");

		public final String display;

		MissileTier(String display) {
			this.display = display;
		}
	}

	public enum MissileFuel {
		SOLID(TextFormatting.GOLD + "Solid Fuel (pre-fueled)", 0),
		ETHANOL_PEROXIDE(TextFormatting.AQUA + "Ethanol / Hydrogen Peroxide", 4_000),
		KEROSENE_PEROXIDE(TextFormatting.BLUE + "Kerosene / Hydrogen Peroxide", 8_000),
		KEROSENE_LOXY(TextFormatting.LIGHT_PURPLE + "Kerosene / Liquid Oxygen", 12_000),
		JETFUEL_LOXY(TextFormatting.RED + "Jet Fuel / Liquid Oxygen", 16_000);

		public final String display;
		public final int defaultCap;

		MissileFuel(String display, int defaultCap) {
			this.display = display;
			this.defaultCap = defaultCap;
		}
	}
}