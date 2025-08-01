package com.hbm.wiaj.cannery;

import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.fluid.IFluidConnector;
import com.hbm.inventory.fluid.FluidType;
import net.minecraft.tileentity.TileEntity;

public class Dummies {

	public static class JarDummyConnector extends TileEntity implements IEnergyConnectorMK2, IFluidConnector {

		@Override public boolean isLoaded() { return false; }
		@Override public long transferFluid(FluidType type, int pressure, long fluid) { return 0; }
		@Override public long getDemand(FluidType type, int pressure) { return 0; }
	}
}
