package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.ILaserable;
import com.hbm.inventory.container.ContainerCoreReceiver;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICoreReceiver;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityCoreReceiver extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IGUIProvider, IFluidStandardReceiver, ILaserable, CompatHandler.OCComponent {

    public long power;
    public long joules;
    //Because it get cleared after the te updates, it needs to be saved here for the container
    public long syncJoules;
    public FluidTankNTM tank;

    public TileEntityCoreReceiver() {
        super(0);
        tank = new FluidTankNTM(Fluids.CRYOGEL, 64000);
    }

    @Override
    public void update() {
        if (!world.isRemote) {

            if ((Long.MAX_VALUE - power) / 5000L < joules)
                power = Long.MAX_VALUE;
            else
                power += joules * 5000L;

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
                this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);

            if (joules > 0) {

                if (tank.getFluidAmount() >= 20) {
                    tank.drain(20, true);
                } else {
                    world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState());
                    return;
                }
            }

            this.networkPackNT(50);

            joules = 0;
        }
    }

    @Override
    public String getName() {
        return "container.dfcReceiver";
    }


    @Override
    public void addEnergy(World world, BlockPos pos, long energy,  EnumFacing dir) {
        // only accept lasers from the front
        if (dir.getOpposite().ordinal() == this.getBlockMetadata()) {
                joules += energy;
        } else {
            world.destroyBlock(pos, false);
            world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 2.5F, true);
        }
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        power = compound.getLong("power");
        joules = compound.getLong("joules");
        tank.readFromNBT(compound, "tank");
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setLong("joules", joules);
        tank.writeToNBT(compound, "tank");
        return super.writeToNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this.getReceivingTanks(), null
            ));
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        buf.writeLong(joules);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        joules = buf.readLong();
        tank.deserialize(buf);
    }


    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return this.power;
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[0];
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return "dfc_receiver";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{joules, getPower()}; //literally only doing this for the consistency between components
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getCryogel(Context context, Arguments args) {
        return new Object[]{tank.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "OpenComputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{joules, getPower(), tank.getFill()};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCoreReceiver(player, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICoreReceiver(player, this);
    }

    //TODO: figure out what this is
//	@Override
//	public void provideExtraInfo(NBTTagCompound data) {
//		data.setDouble(CompatEnergyControl.D_CONSUMPTION_MB, joules > 0 ? 20 : 0);
//		data.setDouble(CompatEnergyControl.D_OUTPUT_HE, joules * 5000);
//	}
}
