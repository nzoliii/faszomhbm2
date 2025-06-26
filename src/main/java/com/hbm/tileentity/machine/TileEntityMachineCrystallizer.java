package com.hbm.tileentity.machine;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import com.hbm.capability.HbmCapability;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.container.ContainerCrystallizer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICrystallizer;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

public class TileEntityMachineCrystallizer extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IFFtoNTMF {

	public long power;
	public static final long maxPower = 1000000;
	public static final int demand = 1000;
	public short progress;
	public short duration = 600;
	public boolean isOn;

	public float angle;
	public float prevAngle;

	public FluidTankNTM tankNew;
	public FluidTank tank;
	private Fluid oldFluid = ModForgeFluids.none;
	private static boolean converted = false;
	public UpgradeManager manager = new UpgradeManager();

	public TileEntityMachineCrystallizer() {
		super(0);
		inventory = new ItemStackHandler(8){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}
			@Override
			public void setStackInSlot(int slot, ItemStack stack) {
				super.setStackInSlot(slot, stack);
				if(stack != null && slot >= 5 && slot <= 6 && stack.getItem() instanceof ItemMachineUpgrade)
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		};
		tankNew = new FluidTankNTM(Fluids.PEROXIDE, 8000);
		tank = new FluidTank(16000);

		converted = true;
	}

	@Override
	public String getName() {
		return "container.crystallizer";
	}

	private void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			this.trySubscribe(tankNew.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	protected DirPos[] getConPos() {

		return new DirPos[] {
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
		};
	}

	@Override
	public void update() {
		if(!converted){
			this.resizeInventory(8);
			convertAndSetFluid(oldFluid, tank, tankNew);
			converted = true;
		}
		manager.eval(inventory, 5, 6);
		if(!world.isRemote) {
			this.isOn = false;

			this.updateConnections();

			power = Library.chargeTEFromItems(inventory, 1, power, maxPower);
			tankNew.setType(7, inventory);
			tankNew.loadTank(3, 4, inventory);

			for(int i = 0; i < getCycleCount(); i++) {

				if(canProcess()) {

					progress++;
					power -= getPowerRequired();
					isOn = true;

					if(progress > getDuration()) {
						progress = 0;
						processItem();

						this.markDirty();
					}

				} else {
					progress = 0;
				}
			}

			this.networkPackNT(25);
		} else {

			prevAngle = angle;

			if(isOn) {
				angle += 5F * this.getCycleCount();

				if(angle >= 360) {
					angle -= 360;
					prevAngle -= 360;
				}
			}
		}

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.add(0.25, 1, 0.25), pos.add(0.75, 6, 0.75)).offset(rot.offsetX * 1.5, 0, rot.offsetZ * 1.5));

		for(EntityPlayer player : players) {
			HbmCapability.IHBMData props = HbmCapability.getData(player);
			if(player == MainRegistry.proxy.me() && !props.getOnLadder()) {
				props.setOnLadder(true);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(progress);
		buf.writeShort(getDuration());
		buf.writeLong(power);
		buf.writeBoolean(isOn);
		tankNew.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		progress = buf.readShort();
		duration = buf.readShort();
		power = buf.readLong();
		isOn = buf.readBoolean();
		tankNew.deserialize(buf);
	}

	private void processItem() {

		CrystallizerRecipes.CrystallizerRecipe result = CrystallizerRecipes.getOutput(inventory.getStackInSlot(0), tankNew.getTankType());

		if(result == null) //never happens but you can't be sure enough
			return;

		ItemStack stack = result.output.copy();

		if(inventory.getStackInSlot(2) == null)
			inventory.setStackInSlot(2, stack);
		else if(inventory.getStackInSlot(2).getCount() + stack.getCount() <= inventory.getStackInSlot(2).getMaxStackSize())
			inventory.getStackInSlot(2).setCount(inventory.getStackInSlot(2).getCount() + stack.getCount());

		tankNew.setFill(tankNew.getFill() - getRequiredAcid(result.acidAmount));

		float freeChance = this.getFreeChance();

		if(freeChance == 0 || freeChance < world.rand.nextFloat())
			this.inventory.getStackInSlot(0).shrink(result.itemAmount);
	}

	private boolean canProcess() {

		//Is there no input?
		if(inventory.getStackInSlot(0).isEmpty())
			return false;

		if(power < getPowerRequired())
			return false;

		CrystallizerRecipes.CrystallizerRecipe result = CrystallizerRecipes.getOutput(inventory.getStackInSlot(0), tankNew.getTankType());

		//Or output?
		if(result == null)
			return false;

		//Not enough of the input item?
		if(inventory.getStackInSlot(0).getCount() < result.itemAmount)
			return false;

		if(tankNew.getFill() < getRequiredAcid(result.acidAmount)) return false;

		ItemStack stack = result.output.copy();

		//Does the output not match?
		if(!inventory.getStackInSlot(2).isEmpty() && (inventory.getStackInSlot(2).getItem() != stack.getItem() || inventory.getStackInSlot(2).getItemDamage() != stack.getItemDamage()))
			return false;

		//Or is the output slot already full?
		if(!inventory.getStackInSlot(2).isEmpty() && inventory.getStackInSlot(2).getCount() + stack.getCount() > inventory.getStackInSlot(2).getMaxStackSize())
			return false;

		return true;
	}

	public int getRequiredAcid(int base) {
		int efficiency = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.EFFECT), 3);
		if(efficiency > 0) {
			return base * (efficiency + 2);
		}
		return base;
	}

	public float getFreeChance() {
		int efficiency = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.EFFECT), 3);
		if(efficiency > 0) {
			return Math.min(efficiency * 0.05F, 0.15F);
		}
		return 0;
	}

	public short getDuration() {
		CrystallizerRecipes.CrystallizerRecipe result = CrystallizerRecipes.getOutput(inventory.getStackInSlot(0), tankNew.getTankType());
		int base = result != null ? result.duration : 600;
		int speed = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
		if(speed > 0) {
			return (short) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.25F)));
		}
		return (short) base;
	}

	public int getPowerRequired() {
		int speed = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 3);
		return (int) (demand + Math.min(speed * 1000, 3000));
	}

	public float getCycleCount() {
		int speed = manager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
		return Math.min(1 + speed * 2, 7);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
		return slot == 0 && CrystallizerRecipes.getOutput(itemStack, this.tankNew.getTankType()) != null;
	}
	
	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot == 2 || slot == 4;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing face) {
		return new int[] { 0, 2, 4 };
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
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		power = nbt.getLong("power");
		if(!converted){
			tank.readFromNBT(nbt.getCompoundTag("tank"));
			oldFluid = tank.getFluid() != null ? tank.getFluid().getFluid() : ModForgeFluids.none;
		} else {
			tankNew.readFromNBT(nbt, "tankNew");
			nbt.removeTag("tank");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setLong("power", power);
		if(!converted){
			nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		} else {
			tankNew.writeToNBT(nbt, "tankNew");
		}
		return nbt;
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / this.getDuration();
	}

	@Override
	public void setPower(long i) {
		this.power = i;
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
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tankNew};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tankNew};
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCrystallizer(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICrystallizer(player.inventory, this);
	}
}