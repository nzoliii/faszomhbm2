package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.MachineDiFurnaceRTG;
import com.hbm.inventory.DiFurnaceRecipes;
import com.hbm.inventory.container.ContainerDiFurnaceRTG;
import com.hbm.inventory.gui.GUIDiFurnaceRTG;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class TileEntityDiFurnaceRTG extends TileEntityMachineBase implements ITickable, ICapabilityProvider, IGUIProvider {

	public int rtgPower;
	public static final int maxRTGPower = 6000;
	public short progress;
	private static final short progressRequired = 2400;
	
	private static final int[] slots_top = new int[] {0, 1};
	private static final int[] slots_bottom = new int[] {2};
	private static final int[] slots_side = new int[] {3, 4, 5, 6, 7, 8};

	private boolean lastTrigger = false;
	
	public TileEntityDiFurnaceRTG() {
		super(9);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.progress = compound.getShort("progress");
		this.rtgPower = compound.getInteger("rtgPower");
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT((NBTTagCompound) compound.getTag("inventory"));
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setShort("progress", progress);
		compound.setInteger("rtgPower", rtgPower);
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote)	{
			rtgPower = Math.min(RTGUtil.updateRTGs(inventory, new int[] {3, 4, 5, 6, 7, 8}), maxRTGPower);

			if (hasPower() && canProcess()) {
				progress += rtgPower;
				if(progress >= progressRequired) {
					processItem();
					progress = 0;
				}
			} else {
				progress = 0;
			}

		
			boolean trigger = isProcessing() || (canProcess() && hasPower());
			if(trigger != lastTrigger)
				MachineDiFurnaceRTG.updateBlockState(trigger, this.world, pos);
			lastTrigger = trigger;

			networkPackNT(10);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeShort(progress);
		buf.writeInt(rtgPower);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		progress = buf.readShort();
		rtgPower = buf.readInt();
	}
	
	@Override
	public String getName(){
		return "container.diFurnaceRTG";
	}
	
	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		int i = e.ordinal();
		return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
	}
	
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 2) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
		if(slot == 0 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(1).getItem() != itemStack.getItem();
		if(slot == 1 && isItemValidForSlot(slot, itemStack)) return inventory.getStackInSlot(0).getItem() != itemStack.getItem();
		return isItemValidForSlot(slot, itemStack);
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot == 2;
	}
	
	public boolean isUsableByPlayer(EntityPlayer player){
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=64;
		}
	}
	
	public int getDiFurnaceProgressScaled(int i) {
		return (progress * i) / progressRequired;
	}
	
	public int getPowerRemainingScaled(int i) {
		return (rtgPower * i) / maxRTGPower;
	}

	public int getPower() {
		return rtgPower;
	}
	
	public boolean canProcess() {
		if(inventory.getStackInSlot(0) == null || inventory.getStackInSlot(1) == null)
		{
			return false;
		}
		ItemStack itemStack = DiFurnaceRecipes.getFurnaceProcessingResult(inventory.getStackInSlot(0), inventory.getStackInSlot(1));
		if(itemStack == null)
		{	
			return false;
		}
		
		if(inventory.getStackInSlot(2) == ItemStack.EMPTY)
		{
			return true;
		}
		if(inventory.getStackInSlot(2).getItem() != ItemStack.EMPTY.getItem() && !inventory.getStackInSlot(2).isItemEqual(itemStack)) {
			return false;
		}
		
		if(inventory.getStackInSlot(2).getCount() < inventory.getSlotLimit(2) && inventory.getStackInSlot(2).getCount() < inventory.getStackInSlot(2).getMaxStackSize()) {
			return true;
		}else{
			return inventory.getStackInSlot(2).getCount() < itemStack.getMaxStackSize();
		}
	}
	
	private void processItem() {
		if(canProcess()) {
			ItemStack itemStack = DiFurnaceRecipes.getFurnaceProcessingResult(inventory.getStackInSlot(0), inventory.getStackInSlot(1));
			
			if(inventory.getStackInSlot(2).isEmpty())
			{
				inventory.setStackInSlot(2, itemStack.copy());
			}else if(inventory.getStackInSlot(2).isItemEqual(itemStack)) {
				inventory.getStackInSlot(2).grow(itemStack.getCount());
			}
			
			for(int i = 0; i < 2; i++)
			{
				if(inventory.getStackInSlot(i).getCount() <= 0)
				{
					inventory.setStackInSlot(i, new ItemStack(inventory.getStackInSlot(i).getItem().setFull3D()));
				}else{
					inventory.getStackInSlot(i).shrink(1);
				}
				if(inventory.getStackInSlot(i).getCount() <= 0)
				{
					inventory.setStackInSlot(i, ItemStack.EMPTY);
				}
			}
			this.markDirty();
		}
	}
	
	public boolean hasPower() {
		return rtgPower > 0;
	}
	
	public boolean isProcessing() {
		return this.progress > 0;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerDiFurnaceRTG(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIDiFurnaceRTG(player.inventory, this);
	}
}