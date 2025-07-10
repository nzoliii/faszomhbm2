package com.hbm.capability;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.config.GeneralConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attaches Forge Energy capabilities to any item that implements IBatteryItem.
 * @author mlbv
 */
public class NTMBatteryCapabilityHandler {

    public static final ResourceLocation HBM_BATTERY_CAPABILITY = new ResourceLocation("hbm", "battery_wrapper");
    public static void initialize() {
        MinecraftForge.EVENT_BUS.register(new NTMBatteryCapabilityHandler());
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty() || !(stack.getItem() instanceof IBatteryItem)) return;
        event.addCapability(HBM_BATTERY_CAPABILITY, new ICapabilityProvider() {
            private NTMBatteryWrapper instance;
            @Override
            public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
                return capability == CapabilityEnergy.ENERGY;
            }
            @Nullable
            @Override
            public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
                if (capability == CapabilityEnergy.ENERGY) {
                    if (instance == null) instance = new NTMBatteryWrapper(stack);
                    return CapabilityEnergy.ENERGY.cast(instance);
                }
                return null;
            }
        });
    }

    public static boolean isBattery(@NotNull ItemStack stack){
    	if(stack.isEmpty()) return false;
        return stack.getItem() instanceof IBatteryItem || stack.hasCapability(CapabilityEnergy.ENERGY, null);
    }

    public static boolean isDischargeableBattery(@NotNull ItemStack stack){
    	if(stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) > 0 && battery.getDischargeRate() > 0;
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) return cap.getEnergyStored() > 0 && cap.canExtract();
        }
        return false;
    }

    public static boolean isChargeableBattery(@NotNull ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getMaxCharge() > battery.getCharge(stack) && battery.getChargeRate() > 0;
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) return cap.getMaxEnergyStored() > cap.getEnergyStored() && cap.canReceive();
        }
        return false;
    }

    public static boolean isEmptyBattery(@NotNull ItemStack stack){
    	if(stack.isEmpty()) return true;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == 0;
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap!= null) return cap.getEnergyStored() == 0;
        }
        return false;
    }

    public static boolean isFullBattery(@NotNull ItemStack stack){
    	if(stack.isEmpty()) return false;
        if (stack.getItem() instanceof IBatteryItem battery) {
            return battery.getCharge(stack) == battery.getMaxCharge();
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap!= null) return cap.getEnergyStored() == cap.getMaxEnergyStored();
        }
        return false;
    }

    /**
     * @return amount of energy charged in HE
     */
    public static long addChargeIfValid(@NotNull ItemStack stack, long heCharge, boolean instant) {
        if (stack.isEmpty()) return 0;
        long added = 0;
        if (stack.getItem() instanceof IBatteryItem battery) {
            long maxcharge = battery.getMaxCharge();
            long charge = battery.getCharge(stack);
            added = instant ? heCharge : Math.min(heCharge, battery.getChargeRate());
            added = charge + added > maxcharge ? maxcharge - charge : added;
            battery.setCharge(stack, charge + added);
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            if (GeneralConfig.conversionRateHeToRF <= 0) return 0;
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) {
                int feReceived = cap.receiveEnergy((int) (heCharge * GeneralConfig.conversionRateHeToRF), false);
                added = (long) (feReceived / GeneralConfig.conversionRateHeToRF);
            }
        }
        return added;
    }

    /**
     * @return The amount of energy that was actually extracted, in HE.
     */
    public static long extractChargeIfValid(@NotNull ItemStack stack, long ntmCharge, boolean instant) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof IBatteryItem battery) {
            long currentCharge = battery.getCharge(stack);
            long extractLimit = instant ? ntmCharge : Math.min(ntmCharge, battery.getDischargeRate());
            long actualExtracted = Math.min(extractLimit, currentCharge);
            if (actualExtracted > 0) {
                battery.setCharge(stack, currentCharge - actualExtracted);
            }
            return actualExtracted;
        } else if (stack.hasCapability(CapabilityEnergy.ENERGY, null)) {
            if (GeneralConfig.conversionRateHeToRF <= 0) {
                return 0;
            }
            IEnergyStorage cap = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (cap != null) {
                int feToExtract = (int) (ntmCharge * GeneralConfig.conversionRateHeToRF);
                int feExtracted = cap.extractEnergy(feToExtract, false);
                return (long) (feExtracted / GeneralConfig.conversionRateHeToRF);
            }
        }
        return 0;
    }
}