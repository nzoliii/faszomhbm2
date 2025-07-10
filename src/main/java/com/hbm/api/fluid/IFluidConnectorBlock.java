package com.hbm.api.fluid;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.ForgeDirection;
import net.minecraft.world.IBlockAccess;

public interface IFluidConnectorBlock {

    /** dir is the face that is connected to, the direction going outwards from the block */
    public boolean canConnect(FluidType type, IBlockAccess world, int x, int y, int z, ForgeDirection dir);
}
