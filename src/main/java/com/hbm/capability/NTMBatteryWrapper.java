package com.hbm.capability;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.config.GeneralConfig;
import net.minecraft.item.ItemStack;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper for NTM batteries to expose them as Forge Energy IEnergyStorage.
 * @author mlbv
 */
public class NTMBatteryWrapper implements IEnergyStorage {

    private final ItemStack stack;
    private final IBatteryItem batteryItem;

    public NTMBatteryWrapper(@NotNull ItemStack stack) {
        if (!(stack.getItem() instanceof IBatteryItem)) { // this should never happen
            throw new IllegalArgumentException("NTMBatteryWrapper can only wrap ItemStacks that have an IBatteryItem.");
        }
        this.stack = stack;
        this.batteryItem = (IBatteryItem) stack.getItem();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || maxReceive <= 0 || GeneralConfig.conversionRateHeToRF <= 0) return 0;
        long powerToOfferInHE = (long) (maxReceive / GeneralConfig.conversionRateHeToRF);
        if (powerToOfferInHE <= 0) return 0;
        long spaceInBattery = batteryItem.getMaxCharge() - batteryItem.getCharge(stack);
        long batteryCanTakeHE = Math.min(spaceInBattery, batteryItem.getChargeRate());
        long actualToReceiveHE = Math.min(powerToOfferInHE, batteryCanTakeHE);
        if (actualToReceiveHE > 0 && !simulate) {
            batteryItem.chargeBattery(stack, actualToReceiveHE);
        }
        long receivedFE = (long) (actualToReceiveHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, receivedFE);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract() || maxExtract <= 0 || GeneralConfig.conversionRateHeToRF <= 0) {
            return 0;
        }
        long maxExtractHE = (long) (maxExtract / GeneralConfig.conversionRateHeToRF);
        if (maxExtractHE <= 0) {
            return 0;
        }
        long batteryCanProvideHE = Math.min(batteryItem.getCharge(stack), batteryItem.getDischargeRate());
        long actualToExtractHE = Math.min(maxExtractHE, batteryCanProvideHE);
        if (actualToExtractHE > 0 && !simulate) {
            batteryItem.dischargeBattery(stack, actualToExtractHE);
        }
        long extractedFE = (long) (actualToExtractHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, extractedFE);
    }

    @Override
    public int getEnergyStored() {
        if (GeneralConfig.conversionRateHeToRF <= 0) return 0;
        long storedFE = (long) (batteryItem.getCharge(stack) * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, storedFE);
    }

    @Override
    public int getMaxEnergyStored() {
        if (GeneralConfig.conversionRateHeToRF <= 0) return 0;
        long maxStoredHE = batteryItem.getMaxCharge();
        long maxStoredFE = (long) (maxStoredHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, maxStoredFE);
    }

    @Override
    public boolean canExtract() {
        return batteryItem.getDischargeRate() > 0;
    }

    @Override
    public boolean canReceive() {
        return batteryItem.getChargeRate() > 0;
    }
}