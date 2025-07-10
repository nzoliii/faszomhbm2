package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.hazard.HazardSystem;
import com.hbm.inventory.StorageDrumRecipes;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ContaminationUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityStorageDrum extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiver {

	public FluidTankNTM[] tanks;
	private static final int[] slots_arr = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 };
	public int age = 0;

	private static final float decayRate = 0.9965402628F; //10s Halflife

	public TileEntityStorageDrum() {
		super(24, 1);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.WASTEFLUID, 16000);
		tanks[1] = new FluidTankNTM(Fluids.WASTEGAS, 16000);
	}

	@Override
	public String getName() {
		return "container.storageDrum";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			float rad = 0;
			
			int liquid = 0;
			int gas = 0;
			
			for(int i = 0; i < 24; i++) {
				
				if(!inventory.getStackInSlot(i).isEmpty()) {

					ItemStack itemStack = inventory.getStackInSlot(i);

					if(world.getTotalWorldTime() % 20 == 0) {
						rad += HazardSystem.getRawRadsFromStack(itemStack);
					}

					int[] wasteData = StorageDrumRecipes.getWaste(inventory.getStackInSlot(i));
					if(wasteData != null){
						if(world.rand.nextInt(wasteData[0]) == 0){
							ItemStack outputStack = StorageDrumRecipes.getOutput(inventory.getStackInSlot(i));
							if(outputStack != null){
								liquid += wasteData[1];
								gas += wasteData[2];
								inventory.setStackInSlot(i, outputStack.copy());
							}
						}
					} else {
						ContaminationUtil.neutronActivateItem(inventory.getStackInSlot(i), 0.0F, decayRate);
					}
				}
			}

			for(int i = 0; i < 2; i++) {
				
				int overflow = Math.max(this.tanks[i].getFluidAmount() + (i == 0 ? liquid : gas) - this.tanks[i].getCapacity(), 0);
				
				if(overflow > 0) {
					RadiationSavedData.incrementRad(world, pos, overflow * 0.5F, Float.MAX_VALUE);
				}
			}
			
			this.tanks[0].fill(Fluids.WASTEFLUID, liquid, true);
			this.tanks[1].fill(Fluids.WASTEGAS, gas, true);
			
			age++;
			
			if(age >= 20)
				age -= 20;
			
			if(age == 9 || age == 19) {
				fillFluidInit(tanks[0]);
			}
			if(age == 8 || age == 18) {
				fillFluidInit(tanks[1]);
			}
			networkPackNT(10);
			if(rad > 0) {
				ContaminationUtil.radiate(world, pos.getZ(), pos.getY(), pos.getX(), 32, rad);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		for(FluidTankNTM tank : this.tanks) {
			tank.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		for(FluidTankNTM tank : this.tanks) {
			tank.deserialize(buf);
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return StorageDrumRecipes.getOutput(itemStack) != null || ContaminationUtil.isContaminated(itemStack);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return !ContaminationUtil.isContaminated(itemStack) && StorageDrumRecipes.getOutput(itemStack) == null;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return slots_arr;
	}

	private void fillFluidInit(FluidTankNTM tank) {
		sendFluid(tank, world, pos.add(-1, 0, 0), ForgeDirection.WEST);
		sendFluid(tank, world, pos.add(1, 0, 0), ForgeDirection.EAST);
		sendFluid(tank, world, pos.add(0, -1, 0), ForgeDirection.DOWN);
		sendFluid(tank, world, pos.add(0, 1, 0), ForgeDirection.UP);
		sendFluid(tank, world, pos.add(0, 0, -1), ForgeDirection.NORTH);
		sendFluid(tank, world, pos.add(0, 0, 1), ForgeDirection.SOUTH);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.tanks[0].readFromNBT(nbt, "liquid");
		this.tanks[1].readFromNBT(nbt,"gas");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		this.tanks[0].writeToNBT(nbt, "liquid");
		this.tanks[1].writeToNBT(nbt,"gas");
		return nbt;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
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
		return super.getCapability(capability, facing);
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return null;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}
}