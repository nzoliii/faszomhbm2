package com.hbm.tileentity.machine;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.ILaserable;
import com.hbm.inventory.DFCRecipes;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.inventory.gui.GUICrateTungsten;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.items.weapon.ItemCrucible;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.AuxParticlePacket;
import com.hbm.packet.BufPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Random;

public class TileEntityCrateTungsten extends TileEntityLockableBase implements IBufPacketReceiver, ITickable, ILaserable, IGUIProvider {

	public ItemStackHandler inventory;

	private Random rand = new Random();

	public int heatTimer = 0;
	public int age = 0;
	public long joules = 0;
	
	public TileEntityCrateTungsten() {
		inventory = new ItemStackHandler(27){
			@Override
			protected void onContentsChanged(int slot){
				markDirty();
			}
		};
	}

	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}

	public boolean canAccess(EntityPlayer player) {
		
		if(!this.isLocked() || player == null) {
			return true;
		} else {
			ItemStack stack = player.getHeldItemMainhand();
			
			if(stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
	        	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			}
			
			if(stack.getItem() == ModItems.key_red) {
	        	world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
				return true;
			}
			
			return this.tryPick(player);
		}
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			if(heatTimer > 0)
				heatTimer--;
	
			if(heatTimer > 0) {
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacket(pos.getX(), pos.getY(), pos.getZ(), 4), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
			}
			age++;
			if(age > 20){
				networkPackNT(150);
				age = 0;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.heatTimer);
		buf.writeLong(this.joules);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.heatTimer = buf.readInt();
		this.joules = buf.readLong();
	}

	public void networkPackNT(int range) {
		if (!world.isRemote)
			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}

	@Override
	public void addEnergy(World world, BlockPos pos, long energy, EnumFacing dir) {
		heatTimer = 5;
		
		for(int i = 0; i < inventory.getSlots(); i++) {
			
			if(inventory.getStackInSlot(i).isEmpty())
				continue;
			
			ItemStack result = FurnaceRecipes.instance().getSmeltingResult(inventory.getStackInSlot(i));

			long requiredEnergy = DFCRecipes.getRequiredFlux(inventory.getStackInSlot(i));
			int count = inventory.getStackInSlot(i).getCount();
			requiredEnergy *= 0.9D;
			if(requiredEnergy > -1 && energy > requiredEnergy){
				if(0.001D > count * rand.nextDouble() * ((double)requiredEnergy/(double)energy)){
					result = DFCRecipes.getOutput(inventory.getStackInSlot(i));
				}
			}
			
			if(inventory.getStackInSlot(i).getItem() == ModItems.crucible && ItemCrucible.getCharges(inventory.getStackInSlot(i)) < 3 && energy > 10000000)
				ItemCrucible.charge(inventory.getStackInSlot(i));
			
			if(result != null && !result.isEmpty()){
				int size = inventory.getStackInSlot(i).getCount();
			
				if(result.getCount() * size <= result.getMaxStackSize()) {
					inventory.setStackInSlot(i, result.copy());
					inventory.getStackInSlot(i).setCount(inventory.getStackInSlot(i).getCount()*size);
				}
			}
		}
		joules = energy;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		if(compound.hasKey("heatTimer"))
			this.heatTimer = compound.getInteger("heatTimer");
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		compound.setInteger("heatTimer", this.heatTimer);
		return super.writeToNBT(compound);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : super.getCapability(capability, facing);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCrateTungsten(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICrateTungsten(player.inventory, this);
	}
}
