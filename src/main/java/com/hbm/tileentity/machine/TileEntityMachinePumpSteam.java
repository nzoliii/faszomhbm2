package com.hbm.tileentity.machine;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import net.minecraft.nbt.NBTTagCompound;

public class TileEntityMachinePumpSteam extends TileEntityMachinePumpBase {

    public FluidTankNTM steam;
    public FluidTankNTM lps;

    public TileEntityMachinePumpSteam() {
        super();
        water = new FluidTankNTM(Fluids.WATER, steamSpeed * 100);
        steam = new FluidTankNTM(Fluids.STEAM, 1_000);
        lps = new FluidTankNTM(Fluids.SPENTSTEAM, 10);
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            for (DirPos pos : getConPos()) {
                this.trySubscribe(steam.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                if (lps.getFill() > 0) {
                    this.sendFluid(lps, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            }
        }
        super.update();
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{water, steam, lps};
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{water, lps};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{steam};
    }

    protected NBTTagCompound getSync() {
        NBTTagCompound data = super.getSync();
        steam.writeToNBT(data, "s");
        lps.writeToNBT(data, "l");
        return data;
    }

    @Override
    public void networkUnpack(NBTTagCompound nbt) {
        super.networkUnpack(nbt);
        steam.readFromNBT(nbt, "s");
        lps.readFromNBT(nbt, "l");
    }

    @Override
    protected boolean canOperate() {
        return steam.getFill() >= 100 && lps.getMaxFill() - lps.getFill() > 0 && water.getFill() < water.getMaxFill();
    }

    @Override
    protected void operate() {
        steam.setFill(steam.getFill() - 100);
        lps.setFill(lps.getFill() + 1);
        int pumpSpeed = water.getTankType() == Fluids.WATER ? steamSpeed : steamSpeed / nonWaterDebuff;
        water.setFill(Math.min(water.getFill() + pumpSpeed, water.getMaxFill()));
    }
}
