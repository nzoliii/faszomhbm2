package com.hbm.capability;

import api.hbm.energymk2.IEnergyHandlerMK2;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import com.hbm.config.GeneralConfig;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * This class is a wrapper for the NTM energy handler, exposing it as a Forge Energy IEnergyStorage.
 * @author mlbv
 */
public class NTMEnergyCapabilityWrapper implements IEnergyStorage {
    private final IEnergyReceiverMK2 receiver;
    private final IEnergyProviderMK2 provider;

    public NTMEnergyCapabilityWrapper(IEnergyHandlerMK2 handler) {
        this.receiver = (handler instanceof IEnergyReceiverMK2) ? (IEnergyReceiverMK2) handler : null;
        this.provider = (handler instanceof IEnergyProviderMK2) ? (IEnergyProviderMK2) handler : null;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive() || maxReceive <= 0 || GeneralConfig.conversionRateHeToRF <= 0) {
            return 0;
        }
        long powerToOfferInHE = (long) (maxReceive / GeneralConfig.conversionRateHeToRF);
        if (powerToOfferInHE <= 0) {
            return 0;
        }
        long leftoverHE = receiver.transferPower(powerToOfferInHE, simulate);
        long receivedHE = powerToOfferInHE - leftoverHE;
        long receivedFE = (long) (receivedHE * GeneralConfig.conversionRateHeToRF);
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
        long availableHE = Math.min(provider.getPower(), provider.getProviderSpeed());
        long actualToExtractHE = Math.min(maxExtractHE, availableHE);
        if (!simulate && actualToExtractHE > 0) {
            provider.usePower(actualToExtractHE);
        }
        long actualToExtractFE = (long) (actualToExtractHE * GeneralConfig.conversionRateHeToRF);

        return (int) Math.min(Integer.MAX_VALUE, actualToExtractFE);
    }

    @Override
    public int getEnergyStored() {
        long storedHE = 0;
        if (provider != null) {
            storedHE = provider.getPower();
        } else if (receiver != null) {
            storedHE = receiver.getPower();
        }

        long storedFE = (long) (storedHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, storedFE);
    }

    @Override
    public int getMaxEnergyStored() {
        long maxStoredHE = 0;
        if (provider != null) {
            maxStoredHE = provider.getMaxPower();
        } else if (receiver != null) {
            maxStoredHE = receiver.getMaxPower();
        }

        long maxStoredFE = (long) (maxStoredHE * GeneralConfig.conversionRateHeToRF);
        return (int) Math.min(Integer.MAX_VALUE, maxStoredFE);
    }

    @Override
    public boolean canExtract() {
        return this.provider != null;
    }

    @Override
    public boolean canReceive() {
        return this.receiver != null;
    }
}