package com.hbm.api.fluid;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;

public interface IFluidStandardTransceiver extends IFluidUser {

    FluidTankNTM[] getSendingTanks();
    FluidTankNTM[] getReceivingTanks();

    @Override
    default long getTotalFluidForSend(FluidType type, int pressure) {

        for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                return tank.getFill();
            }
        }

        return 0;
    }

    @Override
    default void removeFluidForTransfer(FluidType type, int pressure, long amount) {

        for(FluidTankNTM tank : getSendingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                tank.setFill(tank.getFill() - (int) amount);
                return;
            }
        }
    }

    @Override
    default long getDemand(FluidType type, int pressure) {

        for(FluidTankNTM tank : getReceivingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                return tank.getMaxFill() - tank.getFill();
            }
        }

        return 0;
    }

    @Override
    default long transferFluid(FluidType type, int pressure, long amount) {

        for(FluidTankNTM tank : getReceivingTanks()) {
            if(tank.getTankType() == type && tank.getPressure() == pressure) {
                tank.setFill(tank.getFill() + (int) amount);

                if(tank.getFill() > tank.getMaxFill()) {
                    long overshoot = tank.getFill() - tank.getMaxFill();
                    tank.setFill(tank.getMaxFill());
                    return overshoot;
                }

                return 0;
            }
        }

        return amount;
    }
}
