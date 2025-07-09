package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.missile.EntitySoyuz;
import com.hbm.handler.MissileStruct;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemSoyuz;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntitySoyuzLauncher extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardTransceiver {

	public long power;
	public static final long maxPower = 1000000;
	public FluidTankNTM[] tanks;
	//0: sat, 1: cargo
	public byte mode;
	public boolean starting;
	public int countdown;
	public static final int maxCount = 600;
	public byte rocketType = -1;
	
	private AudioWrapper audio;
	
	public MissileStruct load;

	public TileEntitySoyuzLauncher() {
		super(27);
		tanks = new FluidTankNTM[2];
		tanks[0] = new FluidTankNTM(Fluids.KEROSENE, 128000);
		tanks[1] = new FluidTankNTM(Fluids.OXYGEN, 128000);
	}
	
	@Override
	public String getName() {
		return "container.soyuzLauncher";
	}
	
	@Override
	public void update() {
		if (!world.isRemote) {
			if(world.getTotalWorldTime() % 20 == 0) {
				this.updateConnections();
			}
			tanks[0].loadTank(4, 5, inventory);
			tanks[1].loadTank(6, 7, inventory);
			power = Library.chargeTEFromItems(inventory, 8, power, maxPower);
			
			if(!starting || !canLaunch()) {
				countdown = maxCount;
				starting = false;
			} else if(countdown > 0) {
				countdown--;
				
				if(countdown % 100 == 0 && countdown > 0)
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.alarmHatch, SoundCategory.BLOCKS, 100F, 1.1F);
				
			} else {
				liftOff();
			}
			networkPackNT(250);
		}
		
		if(world.isRemote) {
			if(!starting || !canLaunch()) {
				
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
				
				countdown = maxCount;
				
			} else if(countdown > 0) {

				if(audio == null) {
					audio = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.soyuzReady, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 1.0F);
					audio.updateVolume(100);
					audio.startSound();
				}
				
				countdown--;
			}
			
			List<EntitySoyuz> entities = world.getEntitiesWithinAABB(EntitySoyuz.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));
			
			if(!entities.isEmpty()) {
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "smoke");
				data.setString("mode", "shockRand");
				data.setInteger("count", 50);
				data.setDouble("strength", world.rand.nextGaussian() * 3 + 6);
				data.setDouble("posX", pos.getX() + 0.5);
				data.setDouble("posY", pos.getY() - 3);
				data.setDouble("posZ", pos.getZ() + 0.5);
				
				MainRegistry.proxy.effectNT(data);
			}
		}
	}

	private void updateConnections(){
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
		this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ() + dir.offsetX * 10, rot.getOpposite());
		this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ() + dir.offsetX * -9, rot);
	}

	@Override
	public void serialize(ByteBuf buf){
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeByte(mode);
		buf.writeBoolean(starting);
		buf.writeByte(rocketType);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf){
		super.deserialize(buf);
		power = buf.readLong();
		mode = buf.readByte();
		starting = buf.readBoolean();
		rocketType = buf.readByte();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}
	
	@Override
	public void onChunkUnload() {
		if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if(audio != null) {
			audio.stopSound();
			audio = null;
    	}
	}

	public void startCountdown() {
		
		if(canLaunch())
			starting = true;
	}
	
	public void liftOff() {
		
		this.starting = false;
		
		int req = this.getFuelRequired();
		int pow = this.getPowerRequired();
		
		EntitySoyuz soyuz = new EntitySoyuz(world);
		soyuz.setSkin(this.getType());
		soyuz.mode = this.mode;
		soyuz.setLocationAndAngles(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0, 0);
		world.spawnEntity(soyuz);

		world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.soyuzTakeOff, SoundCategory.BLOCKS, 100F, 1.1F);

		tanks[0].drain(req, true);
		tanks[1].drain(req, true);
		power -= pow;
		
		if(mode == 0) {
			soyuz.setSat(inventory.getStackInSlot(2));
			
			if(this.orbital() == 2)
				inventory.setStackInSlot(3, ItemStack.EMPTY);
			
			inventory.setStackInSlot(2, ItemStack.EMPTY);
		}
		
		if(mode == 1) {
			List<ItemStack> payload = new ArrayList<ItemStack>();
			
			for(int i = 9; i < 27; i++) {
				payload.add(inventory.getStackInSlot(i));
				inventory.setStackInSlot(i, ItemStack.EMPTY);
			}

			soyuz.targetX = inventory.getStackInSlot(1).getTagCompound().getInteger("xCoord");
			soyuz.targetZ = inventory.getStackInSlot(1).getTagCompound().getInteger("zCoord");
			soyuz.setPayload(payload);
		}
		
		inventory.setStackInSlot(0, ItemStack.EMPTY);
	}
	
	public boolean canLaunch() {
		
		return hasRocket() && hasFuel() && hasRocket() && hasPower() && designator() != 1 && orbital() != 1 && satellite() != 1;
	}
	
	public boolean hasFuel() {
		
		return tanks[0].getFluidAmount() >= getFuelRequired();
	}
	
	public boolean hasOxy() {

		return tanks[1].getFluidAmount() >= getFuelRequired();
	}
	
	public int getFuelRequired() {
		
		if(mode == 1)
			return 20000 + getDist();
		
		return 128000;
	}
	
	public int getDist() {
		
		if(designator() == 2) {
			int x = inventory.getStackInSlot(1).getTagCompound().getInteger("xCoord");
			int z = inventory.getStackInSlot(1).getTagCompound().getInteger("zCoord");
			
			return (int) Vec3.createVectorHelper(pos.getX() - x, 0, pos.getZ() - z).length();
		}
			
		return 0;
	}
	
	public boolean hasPower() {
		
		return power >= getPowerRequired();
	}
	
	public int getPowerRequired() {
		
		return (int) (maxPower * 0.75);
	}
	
	private byte getType() {
		
		if(!hasRocket())
			return -1;
		Item rocket = inventory.getStackInSlot(0).getItem();
		if(rocket == ModItems.missile_soyuz0)
			return 0;
		if(rocket == ModItems.missile_soyuz1)
			return 1;
		if(rocket == ModItems.missile_soyuz2)
			return 2;
		return 0;
	}
	
	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}
	
	public boolean hasRocket() {
		return inventory.getStackInSlot(0).getItem() instanceof ItemSoyuz;
	}
	
	//0: designator not required
	//1: designator required but not present
	//2: designator present
	public int designator() {
		
		if(mode == 0)
			return 0;
		if((inventory.getStackInSlot(1).getItem() == ModItems.designator || inventory.getStackInSlot(1).getItem() == ModItems.designator_range || inventory.getStackInSlot(1).getItem() == ModItems.designator_manual) && inventory.getStackInSlot(1).hasTagCompound())
			return 2;
		return 1;
	}
	
	//0: sat not required
	//1: sat required but not present
	//2: sat present
	public int satellite() {
		
		if(mode == 1)
			return 0;
		
		if(!inventory.getStackInSlot(2).isEmpty()) {
			return 2;
		}
		return 1;
	}

	//0: module not required
	//1: module required but not present
	//2: module present
	public int orbital() {
		
		if(mode == 1)
			return 0;
		
		if(inventory.getStackInSlot(2).getItem() == ModItems.sat_gerald) {
			if(inventory.getStackInSlot(3).getItem() == ModItems.missile_soyuz_lander)
				return 2;
			return 1;
		}
		return 0;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		power = compound.getLong("power");
		mode = compound.getByte("mode");
		tanks[0].readFromNBT(compound, "tank0");
		tanks[1].readFromNBT(compound, "tank1");
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("power", power);
		compound.setByte("mode", mode);
		tanks[0].writeToNBT(compound, "tank0");
		tanks[1].writeToNBT(compound, "tank1");
		return super.writeToNBT(compound);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
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
	public FluidTankNTM[] getSendingTanks() {
		return null;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}
}
