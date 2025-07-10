package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.inventory.EngineRecipes;
import com.hbm.inventory.EngineRecipes.FuelGrade;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import io.netty.buffer.ByteBuf;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TileEntityMachineDiesel extends TileEntityMachinePolluting implements ITickable, IEnergyProviderMK2, IFluidStandardTransceiver {

	public long power;
	public int soundCycle = 0;
	public static final long maxPower = 50000;
	public long powerCap = 50000;
	public int age = 0;
	public FluidTankNTM tank;

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 2 };

	public static HashMap<FuelGrade, Double> fuelEfficiency = new HashMap<>();
	static {
		fuelEfficiency.put(FuelGrade.MEDIUM,	0.5D);
		fuelEfficiency.put(FuelGrade.HIGH,		0.75D);
		fuelEfficiency.put(FuelGrade.AERO,		0.1D);
	}

	public TileEntityMachineDiesel() {
		super(3, 100);
		tank = new FluidTankNTM(Fluids.NONE, 16000);
	}
	
	@Override
	public String getName() {
		return "container.machineDiesel";
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("powerTime", power);
		compound.setLong("powerCap", powerCap);
		tank.writeToNBT(compound, "tank");
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.power = compound.getLong("powerTime");
		this.powerCap = compound.getLong("powerCap");
		tank.readFromNBT(compound, "tank");
		super.readFromNBT(compound);
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int p_94128_1_ = e.ordinal();
		return p_94128_1_ == 0 ? slots_bottom : (p_94128_1_ == 1 ? slots_top : slots_side);
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / powerCap;
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				this.tryProvide(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
				this.sendSmoke(pos.getX() + dir.offsetX, pos.getX() + dir.offsetY, pos.getX() + dir.offsetZ, dir);
			}

			//Tank Management
			tank.loadTank(0, 1, inventory);
			if(tank.getTankType() == Fluids.NITAN)
				powerCap = maxPower * 10;
			else
				powerCap = maxPower;
			
			// Battery Item
			power = Library.chargeItemsFromTE(inventory, 2, power, powerCap);
			generate();
			this.networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(powerCap);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		power = buf.readLong();
		powerCap = buf.readLong();
		tank.deserialize(buf);
	}

	public boolean hasAcceptableFuel() {
		return getHEFromFuel() > 0;
	}
	
	public long getHEFromFuel() {
		if(tank.getFluid() == null) return 0;
		return getHEFromFuel(tank.getFluid().getFluid());
	}
	
	public static long getHEFromFuel(Fluid type) {
		if(EngineRecipes.hasFuelRecipe(type)) {
			FuelGrade grade = EngineRecipes.getFuelGrade(type);
			double efficiency = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0;
			return (long) (EngineRecipes.getEnergy(type) / 1000L * efficiency);
		}
		
		return 0;
	}

	public void generate() {
		if (hasAcceptableFuel()) {
			if (tank.getFluidAmount() > 0) {
				if (soundCycle == 0) {
					this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_FIREWORK_BLAST, SoundCategory.BLOCKS, 0.5F, -100.0F);
				}
				soundCycle++;

				if (soundCycle >= 5)
					soundCycle = 0;

				tank.drain(1, true);
				if (power + getHEFromFuel() <= powerCap) {
					power += getHEFromFuel();
				} else {
					power = powerCap;
				}
			}
		}
	}
	protected boolean inputValidForTank(int tank, int slot){
		if(!inventory.getStackInSlot(slot).isEmpty()){
			if(isValidFluid(FluidUtil.getFluidContained(inventory.getStackInSlot(slot)))){
				return true;	
			}
		}
		return false;
	}

	private boolean isValidFluid(FluidStack stack) {
		if(stack == null)
			return false;
		return getHEFromFuel(stack.getFluid()) > 0;
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
	public FluidTankNTM[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[]{tank};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[]{tank};
	}
}
