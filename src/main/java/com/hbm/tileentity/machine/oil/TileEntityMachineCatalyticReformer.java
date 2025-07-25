package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.inventory.container.ContainerMachineCatalyticReformer;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineCatalyticReformer;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.items.ModItems;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Tuple;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityMachineCatalyticReformer extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IPersistentNBT, IGUIProvider, IFluidCopiable, ITickable {

    public long power;
    public static final long maxPower = 1_000_000;

    public FluidTankNTM[] tanks;

    public TileEntityMachineCatalyticReformer() {
        super(11);

        this.tanks = new FluidTankNTM[4];
        this.tanks[0] = new FluidTankNTM(Fluids.NAPHTHA, 64_000);
        this.tanks[1] = new FluidTankNTM(Fluids.REFORMATE, 24_000);
        this.tanks[2] = new FluidTankNTM(Fluids.PETROLEUM, 24_000);
        this.tanks[3] = new FluidTankNTM(Fluids.HYDROGEN, 24_000);
    }

    @Override
    public String getName() {
        return "container.catalyticReformer";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            if(this.world.getTotalWorldTime() % 20 == 0) this.updateConnections();
            power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            tanks[0].setType(9, inventory);
            tanks[0].loadTank(1, 2, inventory);

            reform();

            tanks[1].unloadTank(3, 4, inventory);
            tanks[2].unloadTank(5, 6, inventory);
            tanks[3].unloadTank(7, 8, inventory);

            for(DirPos pos : getConPos()) {
                for(int i = 1; i < 4; i++) {
                    if(tanks[i].getFill() > 0) {
                        this.sendFluid(tanks[i], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    }
                }
            }

            networkPackNT(150);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        buf.writeLong(this.power);
        for(int i = 0; i < 4; i++)
            tanks[i].serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        for(int i = 0; i < 4; i++)
            tanks[i].deserialize(buf);
    }

    private void reform() {

        Tuple.Triplet<FluidStack, FluidStack, FluidStack> out = ReformingRecipes.getOutput(tanks[0].getTankType());
        if(out == null) {
            tanks[1].setTankType(Fluids.NONE);
            tanks[2].setTankType(Fluids.NONE);
            tanks[3].setTankType(Fluids.NONE);
            return;
        }

        tanks[1].setTankType(out.getX().type);
        tanks[2].setTankType(out.getY().type);
        tanks[3].setTankType(out.getZ().type);

        if(power < 20_000) return;
        if(tanks[0].getFill() < 100) return;
        if(inventory.getStackInSlot(10).isEmpty() || inventory.getStackInSlot(10).getItem() != ModItems.catalytic_converter) return;

        if(tanks[1].getFill() + out.getX().fill > tanks[1].getMaxFill()) return;
        if(tanks[2].getFill() + out.getY().fill > tanks[2].getMaxFill()) return;
        if(tanks[3].getFill() + out.getZ().fill > tanks[3].getMaxFill()) return;

        tanks[0].setFill(tanks[0].getFill() - 100);
        tanks[1].setFill(tanks[1].getFill() + out.getX().fill);
        tanks[2].setFill(tanks[2].getFill() + out.getY().fill);
        tanks[3].setFill(tanks[3].getFill() + out.getZ().fill);

        power -= 20_000;
    }

    private void updateConnections() {
        for(DirPos pos : getConPos()) {
            this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() + dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 2 - rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 2 - rot.offsetZ, dir),
                new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 2 - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 - rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() + rot.offsetX * 3, pos.getY(), pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(pos.getX() - rot.offsetX * 3, pos.getY(), pos.getZ() - rot.offsetZ * 3, rot.getOpposite())
        };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        power = nbt.getLong("power");
        tanks[0].readFromNBT(nbt, "input");
        tanks[1].readFromNBT(nbt, "o1");
        tanks[2].readFromNBT(nbt, "o2");
        tanks[3].readFromNBT(nbt, "o3");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {

        nbt.setLong("power", power);
        tanks[0].writeToNBT(nbt, "input");
        tanks[1].writeToNBT(nbt, "o1");
        tanks[2].writeToNBT(nbt, "o2");
        tanks[3].writeToNBT(nbt, "o3");
        return super.writeToNBT(nbt);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 7,
                    pos.getZ() + 3
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
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanks[1], tanks[2], tanks[3]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanks[0]};
    }

    @Override
    public boolean canConnect(ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != ForgeDirection.UNKNOWN && dir != ForgeDirection.DOWN;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if(tanks[0].getFill() == 0 && tanks[1].getFill() == 0 && tanks[2].getFill() == 0 && tanks[3].getFill() == 0) return;
        NBTTagCompound data = new NBTTagCompound();
        for(int i = 0; i < 4; i++) this.tanks[i].writeToNBT(data, "" + i);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        for(int i = 0; i < 4; i++) this.tanks[i].readFromNBT(data, "" + i);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineCatalyticReformer(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineCatalyticReformer(player.inventory, this);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this.getReceivingTanks(), this.getSendingTanks())
            );
        }
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
