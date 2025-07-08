package com.hbm.interfaces;

import com.hbm.lib.ForgeDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ILaserable {


    @Deprecated //Use BlockPos damnit, It's there so nobody complains
    public default void addEnergy(World world, int x, int y, int z, long energy, ForgeDirection dir) {
        addEnergy(world, new BlockPos(x, y, z), energy, dir.toEnumFacing());
    }


    public void addEnergy(World world, BlockPos pos, long energy, EnumFacing dir);

}