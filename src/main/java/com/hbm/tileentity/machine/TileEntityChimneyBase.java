package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidUser;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.packet.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public abstract class TileEntityChimneyBase extends TileEntityLoadedBase implements IBufPacketReceiver, ITickable, IFluidUser {

    public long ashTick = 0;
    public long sootTick = 0;
    public int onTicks;

    @Override
    public void update() {

        if(!world.isRemote) {

            if(world.getTotalWorldTime() % 20 == 0) {
                FluidType[] types = new FluidType[] {Fluids.SMOKE, Fluids.SMOKE_LEADED, Fluids.SMOKE_POISON};

                for(FluidType type : types) {
                    this.trySubscribe(type, world, pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X);
                    this.trySubscribe(type, world, pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X);
                    this.trySubscribe(type, world, pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z);
                    this.trySubscribe(type, world, pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z);
                }
            }

            if(ashTick > 0 || sootTick > 0) {

                TileEntity below = world.getTileEntity(pos.add(0, -1, 0));

                if(below instanceof TileEntityAshpit ashpit) {
                    ashpit.ashLevelFly += (int) ashTick;
                    ashpit.ashLevelSoot += (int) sootTick;
                }
                this.ashTick = 0;
                this.sootTick = 0;
            }

            networkPackNT(150);

            if(onTicks > 0) onTicks--;

        } else {

            if(onTicks > 0) {
                this.spawnParticles();
            }
        }
    }

    public boolean cpaturesAsh() {
        return true;
    }

    public boolean cpaturesSoot() {
        return false;
    }

    public void spawnParticles() { }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeInt(onTicks);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        this.onTicks = buf.readInt();
    }

    public void networkPackNT(int range) {
        if (!world.isRemote)
            PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH || dir == ForgeDirection.EAST || dir == ForgeDirection.WEST) &&
                (type == Fluids.SMOKE || type == Fluids.SMOKE_LEADED || type == Fluids.SMOKE_POISON);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long fluid) {

        if(type != Fluids.SMOKE && type != Fluids.SMOKE_LEADED && type != Fluids.SMOKE_POISON) return fluid;

        onTicks = 20;

        if(cpaturesAsh()) ashTick += fluid;
        if(cpaturesSoot()) sootTick += fluid;

        fluid *= getPollutionMod();

        if(type == Fluids.SMOKE) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.SOOT, fluid / 100F);
        if(type == Fluids.SMOKE_LEADED) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.HEAVYMETAL, fluid / 100F);
        if(type == Fluids.SMOKE_POISON) PollutionHandler.incrementPollution(world, pos, PollutionHandler.PollutionType.POISON, fluid / 100F);

        return 0;
    }

    public abstract double getPollutionMod();

    @Override
    public long getDemand(FluidType type, int pressure) {
        return 1_000_000;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[] {};
    }
}
