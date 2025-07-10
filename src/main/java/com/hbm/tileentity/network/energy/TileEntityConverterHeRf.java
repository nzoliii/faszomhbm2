package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import com.hbm.config.GeneralConfig;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = "redstoneflux")})
public class TileEntityConverterHeRf extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2, IEnergyProvider, IEnergyStorage {

	//Thanks to the great people of Fusion Warfare for helping me with the original implementation of the RF energy API

	public TileEntityConverterHeRf() {
		super();
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
				this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
		}
	}
	//RF
	@Override
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(EnumFacing from) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {
		return 0;
	}

	private boolean recursionBrake = false;

	@Optional.Method(modid="redstoneflux")
	public int transferToRFMachine(TileEntity entity, int rf, EnumFacing dir, boolean simulate){
		if(entity instanceof IEnergyReceiver receiver) {
            return receiver.receiveEnergy(dir, rf, simulate);
		}
		return 0;
	}

	public int transferToFEMachine(TileEntity entity, int fe, EnumFacing dir, boolean simulate){
		if(entity != null && entity.hasCapability(CapabilityEnergy.ENERGY, dir)) {
			IEnergyStorage storage = entity.getCapability(CapabilityEnergy.ENERGY, dir);
			if(storage != null && storage.canReceive()){
				return storage.receiveEnergy(fe, simulate);
			}
		}
		return 0;
	}

	//NTM
	@Override
	public long transferPower(long power, boolean simulate) {

		if(recursionBrake)
			return power;

		recursionBrake = true;

		int toRF = (int) Math.min(Integer.MAX_VALUE, power * GeneralConfig.conversionRateHeToRF);
		int transfer;
		int totalTransferred = 0;
		boolean skipRF = false;

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			if (toRF <= 0) break;

			TileEntity entity = world.getTileEntity(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ));
			if (entity == null) continue;

			if(!skipRF){
				try{
					transfer = transferToRFMachine(entity, toRF, dir.getOpposite().toEnumFacing(), simulate);
					totalTransferred += transfer;
					toRF -= transfer;
				} catch (NoSuchMethodError | NoClassDefFoundError e){
					skipRF = true;
				}
			}
			transfer = transferToFEMachine(entity, toRF, dir.getOpposite().toEnumFacing(), simulate);
			totalTransferred += transfer;
			toRF -= transfer;
		}

		recursionBrake = false;
		if(!simulate) {
			lastTransfer = (long)(totalTransferred / GeneralConfig.conversionRateHeToRF);
		}
		return power - (long)(totalTransferred / GeneralConfig.conversionRateHeToRF);
	}

	@Override
	public long getPower() {
		return 0;
	}
	@Override public void setPower(long power) { }

	@Override
	public long getMaxPower() {
		return (long)(Integer.MAX_VALUE / GeneralConfig.conversionRateHeToRF);
	}

	private long lastTransfer = 0;

	@Override
	public long getReceiverSpeed() {

		if(lastTransfer > 0) {
			return lastTransfer * 2;
		} else {
			return getMaxPower();
		}
	}

	//FE
	@Override
	public boolean canExtract(){
		return true;
	}

	@Override
	public boolean canReceive(){
		return false;
	}

	@Override
	public int getMaxEnergyStored(){
		return Integer.MAX_VALUE;
	}

	@Override
	public int getEnergyStored(){
		return 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate){
		return 0;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate){
		return 0;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		if(capability == CapabilityEnergy.ENERGY){
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		if(capability == CapabilityEnergy.ENERGY){
			return (T) this;
		}
		return super.getCapability(capability, facing);
	}
}