package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.packet.BufPacket;
import com.hbm.saveddata.TomSaveData;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;

public class TileEntityCondenser extends TileEntityLoadedBase implements ITickable, IFluidStandardTransceiver, IConfigurableMachine, IFluidCopiable, IFFtoNTMF {

	public int age = 0;
	public FluidTank[] tanksOld;
	public FluidTankNTM[] tanks;
	
	public int waterTimer = 0;
	protected int throughput;
	public boolean vacuumOptimised = false;

	//Configurable values
	public static int inputTankSize = 100;
	public static int outputTankSize = 100;
	private static boolean converted = false;
	
	public TileEntityCondenser() {
		tanksOld = new FluidTank[2];
		//spentsteam
		tanksOld[0] = new FluidTank(100);
		//water
		tanksOld[1] = new FluidTank(100);

		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.SPENTSTEAM, 100);
		tanks[1] = new FluidTankNTM(Fluids.WATER, 100);
		converted = true;
	}

	@Override
	public String getConfigName() {
		return "condenser";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		inputTankSize = IConfigurableMachine.grab(obj, "I:inputTankSize", inputTankSize);
		outputTankSize = IConfigurableMachine.grab(obj, "I:outputTankSize", outputTankSize);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:inputTankSize").value(inputTankSize);
		writer.name("I:outputTankSize").value(outputTankSize);
	}
	
	@Override
	public void update() {
		if (!converted){
			convertAndSetFluids(new Fluid[]{Fluids.SPENTSTEAM.getFF(), FluidRegistry.WATER}, tanksOld, tanks);
			converted = true;
		}
		if(!world.isRemote) {

			age++;
			if(age >= 2) {
				age = 0;
			}

			if(this.waterTimer > 0)
				this.waterTimer--;

			int convert = Math.min(tanks[0].getFill(), tanks[1].getMaxFill() - tanks[1].getFill());
			this.throughput = convert;

			if(extraCondition(convert)) {
				tanks[0].setFill(tanks[0].getFill() - convert);

				if(convert > 0)
					this.waterTimer = 20;

				int light = this.world.getLightFor(EnumSkyBlock.SKY, pos);

				boolean shouldEvaporate = TomSaveData.forWorld(world).fire > 1e-5 && light > 7;
				if(!shouldEvaporate && !vacuumOptimised) {
					CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
					if(CelestialBody.inOrbit(world) || atmosphere == null || atmosphere.getPressure() < 0.01) shouldEvaporate = true;
				}

				if(shouldEvaporate) { // Make both steam and water evaporate during firestorms and in vacuums
					tanks[1].setFill(tanks[1].getFill() - convert);
				} else {
					tanks[1].setFill(tanks[1].getFill() + convert);
				}
			}

			this.subscribeToAllAround(tanks[0].getTankType(), this);
			this.sendFluidToAll(tanks[1], this);

			networkPackNT(150);
		}
	}

	public boolean extraCondition(int convert) { return true; }

	@Override
	public void serialize(ByteBuf buf) {
		this.tanks[0].serialize(buf);
		this.tanks[1].serialize(buf);
		buf.writeByte(this.waterTimer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.tanks[0].deserialize(buf);
		this.tanks[1].deserialize(buf);
		this.waterTimer = buf.readByte();
	}

	public void networkPackNT(int range) {
		if (!world.isRemote)
			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if(!converted){
			tanksOld[0].readFromNBT(nbt.getCompoundTag("steam"));
			tanksOld[1].readFromNBT(nbt.getCompoundTag("water"));
		} else {
			tanks[0].readFromNBT(nbt, "water");
			tanks[1].readFromNBT(nbt, "steam");
			if(nbt.hasKey("water")){
				nbt.removeTag("water");
				nbt.removeTag("steam");
			}
		}
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if(!converted){
			nbt.setTag("steam", tanksOld[0].writeToNBT(new NBTTagCompound()));
			nbt.setTag("water", tanksOld[1].writeToNBT(new NBTTagCompound()));
		} else {
			tanks[0].writeToNBT(nbt, "water");
			tanks[1].writeToNBT(nbt, "steam");
		}
		return super.writeToNBT(nbt);
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tanks [1]};
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tanks [0]};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return null;
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
}