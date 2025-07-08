package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.forgefluid.FFUtils;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.interfaces.Untested;
import com.hbm.inventory.container.ContainerMachineLargeTurbine;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.gui.GUIMachineLargeTurbine;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityMachineLargeTurbine extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver, IGUIProvider, IFFtoNTMF {

    public static final long maxPower = 100000000;
    private static boolean converted = false;
    public long power;
    public int age = 0;
    public FluidTankNTM[] tanksNew;
    public FluidTank[] tanks;
    public Fluid[] types = new Fluid[2];
    public float rotor;
    public float lastRotor;

    public float fanAcceleration = 0F;
    private boolean shouldTurn;
    private AudioWrapper audio;
    private float audioDesync;

    public TileEntityMachineLargeTurbine() {
        super(7);
        tanksNew = new FluidTankNTM[2];
        tanksNew[0] = new FluidTankNTM(Fluids.STEAM, 512000, 0);
        tanksNew[1] = new FluidTankNTM(Fluids.SPENTSTEAM, 10240000, 1);

        tanks = new FluidTank[2];
        tanks[0] = new FluidTank(512000);
        tanks[1] = new FluidTank(10240000);
        types[0] = Fluids.STEAM.getFF();
        ;
        types[1] = Fluids.SPENTSTEAM.getFF();
        ;

        Random rand = new Random();
        audioDesync = rand.nextFloat() * 0.05F;

        converted = true;
    }

    @Untested
    @Override
    public void update() {
        if (!world.isRemote) {
            if (!converted) {
                convertAndSetFluids(types, tanks, tanksNew);
                converted = true;
            }
            age++;
            if (age >= 2) {
                age = 0;
            }

            ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
            this.tryProvide(world, pos.getX() + dir.offsetX * -4, pos.getY(), pos.getZ() + dir.offsetZ * -4, dir.getOpposite());
            for (DirPos pos : getConPos())
                this.trySubscribe(tanksNew[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            for (DirPos pos : getConPos())
                this.sendFluid(tanksNew[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());

            tanksNew[0].setType(0, 1, inventory);
            tanksNew[0].loadTank(2, 3, inventory);

            power = Library.chargeItemsFromTE(inventory, 4, power, maxPower);

            boolean operational = false;

            FluidType in = tanksNew[0].getTankType();
            boolean valid = false;
            if (in.hasTrait(FT_Coolable.class)) {
                FT_Coolable trait = in.getTrait(FT_Coolable.class);
                double eff = trait.getEfficiency(FT_Coolable.CoolingType.TURBINE); //100% efficiency
                if (eff > 0) {
                    tanksNew[1].setTankType(trait.coolsTo);
                    int inputOps = (int) Math.floor(tanksNew[0].getFill() / trait.amountReq); //amount of cycles possible with the entire input buffer
                    int outputOps = (tanksNew[1].getMaxFill() - tanksNew[1].getFill()) / trait.amountProduced; //amount of cycles possible with the output buffer's remaining space
                    int cap = (int) Math.ceil(tanksNew[0].getFill() / trait.amountReq / 5F); //amount of cycles by the "at least 20%" rule
                    int ops = Math.min(inputOps, Math.min(outputOps, cap)); //defacto amount of cycles
                    tanksNew[0].setFill(tanksNew[0].getFill() - ops * trait.amountReq);
                    tanksNew[1].setFill(tanksNew[1].getFill() + ops * trait.amountProduced);
                    this.power += (ops * trait.heatEnergy * eff);
                    valid = true;
                    operational = ops > 0;
                }
            }
            if (!valid) tanksNew[1].setTankType(Fluids.NONE);
            if (power > maxPower) power = maxPower;

            tanksNew[1].unloadTank(5, 6, inventory);

            for (int i = 0; i < 2; i++)
                tanksNew[i].updateTank(pos.getX(), pos.getY(), pos.getZ(), world.provider.getDimension());

            NBTTagCompound data = new NBTTagCompound();
            data.setLong("power", power);
            data.setBoolean("operational", operational);
            this.networkPack(data, 50);
        } else {
            this.lastRotor = this.rotor;
            this.rotor += this.fanAcceleration;

            if (this.rotor >= 360) {
                this.rotor -= 360;
                this.lastRotor -= 360;
            }

            if (shouldTurn) {
                // Fan accelerates with a random offset to ensure the audio doesn't perfectly align, makes for a more pleasant hum
                this.fanAcceleration = Math.max(0F, Math.min(15F, this.fanAcceleration += 0.075F + audioDesync));

                if (audio == null) {
                    audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.turbofanOperate, SoundCategory.BLOCKS, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ(), 1.0F, 10F);
                    audio.startSound();
                }

                float turbineSpeed = this.fanAcceleration / 15F;
                audio.updateVolume(getVolume(0.4f * turbineSpeed));
                audio.updatePitch(0.25F + 0.75F * turbineSpeed);
            } else {
                this.fanAcceleration = Math.max(0F, Math.min(15F, this.fanAcceleration -= 0.1F));

                if (audio != null) {
                    if (this.fanAcceleration > 0) {
                        float turbineSpeed = this.fanAcceleration / 15F;
                        audio.updateVolume(getVolume(0.4f * turbineSpeed));
                        audio.updatePitch(0.25F + 0.75F * turbineSpeed);
                    } else {
                        audio.stopSound();
                        audio = null;
                    }
                }
            }
        }
    }

    protected DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot),
                new DirPos(pos.getX() - rot.offsetX * 2, pos.getY(), pos.getZ() - rot.offsetZ * 2, rot.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 2, pos.getY(), pos.getZ() + dir.offsetZ * 2, dir)
        };
    }

    @Override
    public void networkUnpack(NBTTagCompound data) {
        super.networkUnpack(data);

        this.power = data.getLong("power");
        this.shouldTurn = data.getBoolean("operational");
    }

    public long getPowerScaled(int i) {
        return (power * i) / maxPower;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!converted) {
            if (nbt.hasKey("tankType0"))
                types[0] = FluidRegistry.getFluid(nbt.getString("tankType0"));
            else
                types[0] = null;
            if (nbt.hasKey("tankType1"))
                types[1] = FluidRegistry.getFluid(nbt.getString("tankType1"));
            else
                types[1] = null;

            FFUtils.deserializeTankArray(nbt.getTagList("tanks", 10), tanks);
        } else {
            tanksNew[0].readFromNBT(nbt, "water");
            tanksNew[1].readFromNBT(nbt, "steam");
            if (nbt.hasKey("tankType0")) {
                nbt.removeTag("tankType0");
                nbt.removeTag("tankType1");
                nbt.removeTag("tanks");
            }
        }
        power = nbt.getLong("power");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (!converted) {
            nbt.setTag("tanks", FFUtils.serializeTankArray(tanks));
            if (types[0] != null)
                nbt.setString("tankType0", types[0].getName());
            if (types[1] != null)
                nbt.setString("tankType1", types[1].getName());
        } else {
            tanksNew[0].writeToNBT(nbt, "water");
            tanksNew[1].writeToNBT(nbt, "steam");
        }
        nbt.setLong("power", power);
        return nbt;
    }

    @Override
    public String getName() {
        return "container.machineLargeTurbine";
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
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanksNew;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{tanksNew[1]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tanksNew[0]};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineLargeTurbine(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineLargeTurbine(player.inventory, this);
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
