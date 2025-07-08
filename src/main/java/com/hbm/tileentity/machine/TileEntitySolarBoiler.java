package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.BufPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntitySolarBoiler extends TileEntityLoadedBase implements IBufPacketReceiver, ITickable, IFluidStandardTransceiver {

    public FluidTankNTM[] tanks;
    public int heat;
    public int heatInput;
    public static int maxHeat = 320_000; //the heat required to turn 64k of water into steam
    public static final double diffusion = 0.1D;

    public TileEntitySolarBoiler() {
        super();
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.WATER, 16000);
        tanks[1] = new FluidTankNTM(Fluids.STEAM, 1600000);
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            tryConvert();
            
            fillFluidInit(tanks[1]);
            heat += heatInput;
            if(heat > maxHeat) heat = maxHeat;
            networkPackNT(25);
            heat *= 0.999;
            heatInput = 0;
        }
    }

    public void fillFluidInit(FluidTankNTM tank) {
        sendFluid(tank, world, pos.up(3), ForgeDirection.UP);
        sendFluid(tank, world, pos.down(), ForgeDirection.DOWN);
	}

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this.getReceivingTanks(), this.getSendingTanks())
            );
        }
        return super.getCapability(capability, facing);
    }

    public void networkPackNT(int range) {
        if (!world.isRemote)
            PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heat = nbt.getInteger("heat");
        tanks[0].readFromNBT(nbt, "tank0");
        tanks[1].readFromNBT(nbt, "tank1");
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("heat", heat);
        tanks[0].writeToNBT(nbt, "tank0");
        tanks[1].writeToNBT(nbt, "tank1");
        return nbt;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(heat);
        buf.writeInt(heatInput);
        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.heatInput = buf.readInt();
        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
    }

    private void tryConvert() {

        int process = heat / 50;
        process = Math.min(process, tanks[0].getFill());
        process = Math.min(process, (tanks[1].getMaxFill() - tanks[1].getFill()) / 100);
            
        tanks[0].drain(process, true);
        tanks[1].fill(Fluids.STEAM, process * 100, true);
        heat = 0;
    }

    AxisAlignedBB bb = null;

    @NotNull
    @Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 3,
					pos.getZ() + 2
					);
		}

		return bb;
	}

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanks[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanks[0]};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }
}