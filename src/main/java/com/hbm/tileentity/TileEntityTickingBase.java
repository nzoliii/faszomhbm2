package com.hbm.tileentity;

import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidTank;

public abstract class TileEntityTickingBase extends TileEntityLoadedBase implements ITickable {
	
	public abstract String getInventoryName();
	
	public int getGaugeScaled(int i, FluidTank tank) {
		return tank.getFluidAmount() * i / tank.getCapacity();
	}
}
