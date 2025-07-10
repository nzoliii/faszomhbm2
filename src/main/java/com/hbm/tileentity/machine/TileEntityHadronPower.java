package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityTickingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

public class TileEntityHadronPower extends TileEntityTickingBase implements IEnergyReceiverMK2 {

	public long power;
	public static final long maxPower = 1000000000;

	@Override
	public void update() {
		if(!world.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
			NBTTagCompound data = new NBTTagCompound();
			data.setLong("power", power);
			this.networkPack(data, 15);
		}
	}

	@Override
	public String getInventoryName(){
		return "Hadron Power Thing";
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {
		this.power = nbt.getLong("power");
	}
	
	@Override
	public void setPower(long i) {
		power = i;
		this.markDirty();
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound){
		compound.setLong("power", power);
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound){
		power = compound.getLong("power");
		super.readFromNBT(compound);
	}

	@Override
	public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.cast(
					new NTMEnergyCapabilityWrapper(this)
			);
		}
		return super.getCapability(capability, facing);
	}
}
