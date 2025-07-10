package com.hbm.api.fluid;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.util.Compat;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public interface IFluidUser extends IFluidConnector {

    default void sendFluid(FluidTankNTM tank, World world, BlockPos pos, ForgeDirection dir) {
        sendFluid(tank.getTankType(), tank.getPressure(), world, pos.getX(), pos.getY(), pos.getZ(), dir);
    }

    default void sendFluid(FluidTankNTM tank, World world, int x, int y, int z, ForgeDirection dir) {
        sendFluid(tank.getTankType(), tank.getPressure(), world, x, y, z, dir);
    }

    default void sendFluid(FluidType type, int pressure, World world, int x, int y, int z, ForgeDirection dir) {

        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        boolean wasSubscribed = false;
        boolean red = false;

        if(te instanceof IFluidConductor con) {

            if(con.getPipeNet(type) != null && con.getPipeNet(type).isSubscribed(this)) {
                con.getPipeNet(type).unsubscribe(this);
                wasSubscribed = true;
            }
        }

        if(te instanceof IFluidConnector con) {

            if(con.canConnect(type, dir.getOpposite())) {
                long toSend = this.getTotalFluidForSend(type, pressure);

                if(toSend > 0) {
                    long transfer = toSend - con.transferFluid(type, pressure, toSend);
                    this.removeFluidForTransfer(type, pressure, transfer);
                }
                red = true;
            }
        }

        if(wasSubscribed) {
            IFluidConductor con = (IFluidConductor) te;
            if (con.getPipeNet(type) != null && !con.getPipeNet(type).isSubscribed(this)) {
                con.getPipeNet(type).subscribe(this);
            }
        }

        if(particleDebug) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "network");
            data.setString("mode", "fluid");
            data.setInteger("color", type.getColor());
            double posX = x + 0.5 - dir.offsetX * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posY = y + 0.5 - dir.offsetY * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            double posZ = z + 0.5 - dir.offsetZ * 0.5 + world.rand.nextDouble() * 0.5 - 0.25;
            data.setDouble("mX", dir.offsetX * (red ? 0.025 : 0.1));
            data.setDouble("mY", dir.offsetY * (red ? 0.025 : 0.1));
            data.setDouble("mZ", dir.offsetZ * (red ? 0.025 : 0.1));
            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, posX, posY, posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 25));
        }
    }

    static IPipeNet getPipeNet(World world, int x, int y, int z, FluidType type) {

        TileEntity te = Compat.getTileStandard(world, x, y, z);

        if(te instanceof IFluidConductor) {
            IFluidConductor con = (IFluidConductor) te;

            if(con.getPipeNet(type) != null) {
                return con.getPipeNet(type);
            }
        }

        return null;
    }

    /** Use more common conPos method instead */
    @Deprecated default void sendFluidToAll(FluidTankNTM tank, TileEntity te) {
        sendFluidToAll(tank.getTankType(), tank.getPressure(), te);
    }

    /** Use more common conPos method instead */
    @Deprecated default void sendFluidToAll(FluidType type, int pressure, TileEntity te) {

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            sendFluid(type, pressure, te.getWorld(), te.getPos().getX() + dir.offsetX, te.getPos().getY() + dir.offsetY, te.getPos().getZ() + dir.offsetZ, dir);
        }
    }

    default long getTotalFluidForSend(FluidType type, int pressure) { return 0; }
    default void removeFluidForTransfer(FluidType type, int pressure, long amount) { }

    default void subscribeToAllAround(FluidType type, TileEntity te) {
        subscribeToAllAround(type, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
    }

    default void subscribeToAllAround(FluidType type, World world, int x, int y, int z) {

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.trySubscribe(type, world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir);
    }

    default void unsubscribeToAllAround(FluidType type, TileEntity te) {
        unsubscribeToAllAround(type, te.getWorld(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
    }

    default void unsubscribeToAllAround(FluidType type, World world, int x, int y, int z) {

        for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
            this.tryUnsubscribe(type, world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
    }

    /**
     * Returns all internal tanks of this tile. Not used by the fluid network, it should only be used for display purposes or edge cases that can't be solved otherwise.
     * The array is either composed of the original tank or outright the original tank array, so changes done to this array will extend to the IFluidUser.
     * @return
     */
    FluidTankNTM[] getAllTanks();
}
