package com.hbm.items.tool;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFluidContainerInfinite extends Item {

	@Nullable
	private final FluidType type;
	private final int amount;
	private final int chance;

	public ItemFluidContainerInfinite(@Nullable FluidType type, int amount, String s) {
		this(type, amount, 1, s);
	}
	
	public ItemFluidContainerInfinite(@Nullable FluidType type, int amount, int chance, String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.type = type;
		this.amount = amount;
		this.chance = chance;
		
		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World world, @NotNull List<String> list, @NotNull ITooltipFlag flagIn){
		super.addInformation(stack, world, list, flagIn);
		list.add(I18nUtil.resolveKey("desc.canisterinfinite", amount * 0.02F));
	}

	@Nullable
	public FluidType getType() { return this.type; }
	public int getAmount() { return this.amount; }
	public int getChance() { return this.chance; }
	public boolean allowPressure(int pressure) { return this == ModItems.fluid_barrel_infinite || pressure == 0; }
}
