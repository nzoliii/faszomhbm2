package com.hbm.tileentity.machine;

import com.hbm.api.block.IDrillInteraction;
import com.hbm.api.block.IMiningDrill;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.gas.BlockGasBase;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.CentrifugeRecipes;
import com.hbm.inventory.ShredderRecipes;
import com.hbm.inventory.UpgradeManager;
import com.hbm.inventory.container.ContainerMachineMiningLaser;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineMiningLaser;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.LoopedSoundPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class TileEntityMachineMiningLaser extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardSender, IMiningDrill, IFFtoNTMF, IGUIProvider {

	public long power;
	public static final long maxPower = 100000000;
	public static final int consumption = 10000;
	public FluidTankNTM tankNew;
	public FluidTank tank;

	public boolean isOn;
	public int targetX;
	public int targetY;
	public int targetZ;
	public int lastTargetX;
	public int lastTargetY;
	public int lastTargetZ;
	public boolean beam;
	boolean lock = false;
	double breakProgress;
	private UpgradeManager manager;
	private static boolean converted = false;
	double clientBreakProgress = 0;

	public TileEntityMachineMiningLaser() {
		super(0);
		//slot 0: battery
		//slots 1 - 8: upgrades
		//slots 9 - 29: output
		inventory = new ItemStackHandler(30){
			@Override
			protected void onContentsChanged(int slot) {
				super.onContentsChanged(slot);
				markDirty();
			}
			@Override
			public void setStackInSlot(int slot, ItemStack stack) {
				super.setStackInSlot(slot, stack);
				if(stack != null && slot >= 1 && slot <= 8 && stack.getItem() instanceof ItemMachineUpgrade)
					world.playSound(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		};
		tankNew = new FluidTankNTM(Fluids.OIL, 64000, 0);
		tank = new FluidTank(64000);
		manager = new UpgradeManager();
		converted = true;
	}

	@Override
	public String getName() {
		return "container.miningLaser";
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(!converted){
				convertAndSetFluid(Fluids.OIL.getFF(), tank, tankNew);
				converted = true;
			}
			this.trySubscribe(world, pos.getX(), pos.getY() + 2, pos.getZ(), ForgeDirection.UP);

			this.sendFluid(tankNew, world, pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X);
			this.sendFluid(tankNew, world, pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X);
			this.sendFluid(tankNew, world, pos.getX(), pos.getY() + 2, pos.getZ(), Library.POS_Z);
			this.sendFluid(tankNew, world, pos.getX(), pos.getY() - 2, pos.getZ(), Library.NEG_Z);

			power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
			tankNew.updateTank(pos.getX(), pos.getY(), pos.getZ(), this.world.provider.getDimension());

			//reset progress if the position changes
			if(lastTargetX != targetX ||
					lastTargetY != targetY ||
					lastTargetZ != targetZ)
				breakProgress = 0;

			//set last positions for interpolation and the like
			lastTargetX = targetX;
			lastTargetY = targetY;
			lastTargetZ = targetZ;
			
			if(isOn) {

				manager.eval(inventory, 1, 8);
				int cycles = 1 + manager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
				int speed = 1 + Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 12);
				int range = 1 + Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.EFFECT) * 2, 24);
				int fortune = Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.FORTUNE), 3);
				int consumption = this.consumption
						- (this.consumption * Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 12) / 16)
						+ (this.consumption * Math.min(manager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED), 12) / 16);

				if(doesScream()){
					cycles *= 4;
					speed *= 4;
					consumption *= 20;
				}

				for(int i = 0; i < cycles; i++) {

					if(power < consumption) {
						beam = false;
						break;
					}

					power -= consumption;

					if(targetY <= 0)
						targetY = pos.getY() - 2;

					scan(range);

					IBlockState block = world.getBlockState(new BlockPos(targetX, targetY, targetZ));

					if(block.getMaterial().isLiquid()) {
						world.setBlockToAir(new BlockPos(targetX, targetY, targetZ));
						buildDam();
						continue;
					}
					if(beam && canBreak(block, targetX, targetY, targetZ)) {

						breakProgress += getBreakSpeed(speed);
						clientBreakProgress = Math.min(breakProgress, 1);

						if(breakProgress < 1) {
							world.sendBlockBreakProgress(-1, new BlockPos(targetX, targetY, targetZ), (int) Math.floor(breakProgress * 10));
						} else {
							breakBlock(fortune);
							buildDam();
						}
					}
				}
				if(doesScream()) {
					world.playSound(null, targetX + 0.5, targetY + 0.5, targetZ + 0.5, HBMSoundHandler.scream, SoundCategory.BLOCKS, 20.0F, 1.0F);
				}
			} else {
				targetY = pos.getY() - 2;
				beam = false;
			}
			
			this.tryFillContainer(pos.getX() + 2, pos.getY(), pos.getZ());
			this.tryFillContainer(pos.getX() - 2, pos.getY(), pos.getZ());
			this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ() + 2);
			this.tryFillContainer(pos.getX(), pos.getY(), pos.getZ() - 2);

			PacketThreading.createSendToAllThreadedPacket(new LoopedSoundPacket(pos.getX(), pos.getY(), pos.getZ()));
			networkPackNT(250);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeLong(power);
		buf.writeInt(lastTargetX);
		buf.writeInt(lastTargetY);
		buf.writeInt(lastTargetZ);
		buf.writeInt(targetX);
		buf.writeInt(targetY);
		buf.writeInt(targetZ);
		buf.writeBoolean(beam);
		buf.writeBoolean(isOn);
		buf.writeDouble(clientBreakProgress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.lastTargetX = buf.readInt();
		this.lastTargetY = buf.readInt();
		this.lastTargetZ = buf.readInt();
		this.targetX = buf.readInt();
		this.targetY = buf.readInt();
		this.targetZ = buf.readInt();
		this.beam = buf.readBoolean();
		this.isOn = buf.readBoolean();
		this.breakProgress = buf.readDouble();
	}
	
	private void buildDam() {

		placeBags(new BlockPos(targetX + 1, targetY, targetZ));
		placeBags(new BlockPos(targetX - 1, targetY, targetZ));
		placeBags(new BlockPos(targetX, targetY, targetZ + 1));
		placeBags(new BlockPos(targetX, targetY, targetZ - 1));
	}

	private void placeBags(BlockPos wallPos){
		IBlockState bState = world.getBlockState(wallPos);
		if(bState.getBlock().isReplaceable(world, wallPos) && bState.getMaterial().isLiquid())
			world.setBlockState(wallPos, ModBlocks.barricade.getDefaultState());
	}
	
	private void tryFillContainer(int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))
			return;
		IItemHandler h = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(!(h instanceof IItemHandlerModifiable))
			return;
		
		IItemHandlerModifiable inv = (IItemHandlerModifiable)h;
		
		for(int i = 9; i <= 29; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {
				int prev = inventory.getStackInSlot(i).getCount();
				inventory.setStackInSlot(i, InventoryUtil.tryAddItemToInventory(inv, 0, inv.getSlots() - 1, inventory.getStackInSlot(i)));

				if(inventory.getStackInSlot(i).isEmpty() || inventory.getStackInSlot(i).getCount() < prev)
					return;
			}
		}
	}

	private void breakBlock(int fortune) {
		IBlockState state = world.getBlockState(new BlockPos(targetX, targetY, targetZ));
		Block b = state.getBlock();
		boolean normal = true;
		boolean doesBreak = true;

		if(b == Blocks.LIT_REDSTONE_ORE){
			b = Blocks.REDSTONE_ORE;
			state = Blocks.REDSTONE_ORE.getDefaultState();
		}

		ItemStack stack = new ItemStack(b, 1, b.getMetaFromState(state));

		if(stack != ItemStack.EMPTY && stack.getItem() != null) {
			if(hasCrystallizer()) {

				CrystallizerRecipes.CrystallizerRecipe result = CrystallizerRecipes.getOutput(stack, Fluids.PEROXIDE);
				if(result == null) result = CrystallizerRecipes.getOutput(stack, Fluids.SULFURIC_ACID);
				if(result != null) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.output.copy()));
					normal = false;
				}

			} else if(hasCentrifuge()) {

				ItemStack[] result = CentrifugeRecipes.getOutput(stack);
				if(result != null) {
					for(ItemStack sta : result) {

						if(sta != null) {
							world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, sta.copy()));
							normal = false;
						}
					}
				}

			} else if(hasShredder()) {

				ItemStack result = ShredderRecipes.getShredderResult(stack);
				if(result != null && result.getItem() != ModItems.scrap) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}

			} else if(hasSmelter()) {

				ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
				if(result != null) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, result.copy()));
					normal = false;
				}
			}
		}
		
		if(normal && b instanceof IDrillInteraction) {
			IDrillInteraction in = (IDrillInteraction) b;
			doesBreak = in.canBreak(world, targetX, targetY, targetZ, state, this);
			if(doesBreak){
				ItemStack drop = in.extractResource(world, targetX, targetY, targetZ, state, this);
				
				if(drop != null) {
					world.spawnEntity(new EntityItem(world, targetX + 0.5, targetY + 0.5, targetZ + 0.5, drop.copy()));
				}
			}
		}

		if(doesBreak){
			if(normal)
				b.dropBlockAsItem(world, new BlockPos(targetX, targetY, targetZ), state, fortune);
			world.destroyBlock(new BlockPos(targetX, targetY, targetZ), false);
		}
		
		suckDrops();

		breakProgress = 0;
	}

	//hahahahahahahaha he said "suck"
	private void suckDrops() {

		int rangeHor = 3;
		int rangeVer = 1;
		boolean nullifier = hasNullifier();

		List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(
				targetX + 0.5 - rangeHor,
				targetY + 0.5 - rangeVer,
				targetZ + 0.5 - rangeHor,
				targetX + 0.5 + rangeHor,
				targetY + 0.5 + rangeVer,
				targetZ + 0.5 + rangeHor
				));

		for(EntityItem item : items) {

			if(nullifier && ItemMachineUpgrade.scrapItems.contains(item.getItem().getItem())) {
				item.setDead();
				continue;
			}
			
			if(item.getItem().getItem() == Item.getItemFromBlock(ModBlocks.ore_oil)) {

				tankNew.setTankType(Fluids.OIL); //just to be sure

				tankNew.setFill(tankNew.getFill() + 500);
				if(tankNew.getFill() > tankNew.getMaxFill())
					tankNew.setFill(tankNew.getMaxFill());

				item.setDead();
				continue;
			}

			ItemStack stack = InventoryUtil.tryAddItemToInventory(inventory, 9, 29, item.getItem().copy());

			if(stack == null)
				item.setDead();
			else
				item.setItem(stack.copy()); //copy is not necessary but i'm paranoid due to the kerfuffle of the old drill
		}

		List<EntityLivingBase> mobs = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(
				targetX + 0.5 - 1,
				targetY + 0.5 - 1,
				targetZ + 0.5 - 1,
				targetX + 0.5 + 1,
				targetY + 0.5 + 1,
				targetZ + 0.5 + 1
				));

		for(EntityLivingBase mob : mobs) {
			mob.setFire(5);
		}
	}

	public double getBreakSpeed(int speed) {

		float hardness = world.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlockHardness(world, new BlockPos(targetX, targetY, targetZ)) * 15 / speed;
		if(hardness == 0)
			return 1;

		return 1 / hardness;
	}

	public void scan(int range) {

		for(int x = -range; x <= range; x++) {
			for(int z = -range; z <= range; z++) {

				if(world.getBlockState(new BlockPos(x + pos.getX(), targetY, z + pos.getZ())).getMaterial().isLiquid()) {
					/*targetX = x + xCoord;
					targetZ = z + zCoord;
					world.func_147480_a(x + xCoord, targetY, z + zCoord, false);
					beam = true;*/

					continue;
				}

				if(canBreak(world.getBlockState(new BlockPos(x + pos.getX(), targetY, z + pos.getZ())), x + pos.getX(), targetY, z + pos.getZ())) {
					targetX = x + pos.getX();
					targetZ = z + pos.getZ();
					beam = true;
					return;
				}
			}
		}

		beam = false;
		targetY--;
	}

	private boolean canBreak(IBlockState block, int x, int y, int z) {
		Block b = block.getBlock();
		if(b == Blocks.AIR) return false;
		if(b == Blocks.BEDROCK) return false;
		if(b instanceof BlockGasBase) return false;
		float hardness = block.getBlockHardness(world, new BlockPos(x, y, z));
		if(hardness < 0 || hardness > 3_500_000) return false;
		if(block.getMaterial().isLiquid()) return false;
		return true;
	}

	public int getRange() {

		int range = 1;

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {
				
				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_1)
					range += 2;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_2)
					range += 4;
				else if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_effect_3)
					range += 6;
			}
		}

		return Math.min(range, 25);
	}

	public boolean hasNullifier() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_nullifier)
					return true;
			}
		}

		return false;
	}

	public boolean hasSmelter() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_smelter)
					return true;
			}
		}

		return false;
	}

	public boolean hasShredder() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_shredder)
					return true;
			}
		}

		return false;
	}

	public boolean hasCentrifuge() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_centrifuge)
					return true;
			}
		}

		return false;
	}

	public boolean hasCrystallizer() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_crystallizer)
					return true;
			}
		}

		return false;
	}

	public boolean doesScream() {

		for(int i = 1; i < 9; i++) {

			if(!inventory.getStackInSlot(i).isEmpty()) {

				if(inventory.getStackInSlot(i).getItem() == ModItems.upgrade_scream)
					return true;
			}
		}

		return false;
	}
	
	public int getWidth() {

		return 1 + getRange() * 2;
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
	
	public int getPowerScaled(int i) {
		return (int)((power * i) / maxPower);
	}

	public int getProgressScaled(int i) {
		return (int) (breakProgress * i);
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, int j) {
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, int j) {
		return i >= 9 && i <= 29;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing slot) {

		int[] slots = new int[21];

		for(int i = 0; i < 21; i++) {
			slots[i] = i + 9;
		}

		return slots;
	}

	@Override
	public void setPower(long i) {
		power = i;
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
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if(!converted) {
			tank.readFromNBT(compound.getCompoundTag("tank"));
		} else {
			tankNew.readFromNBT(compound, "oil");
			if(compound.hasKey("tank")) compound.removeTag("tank");
		}
		isOn = compound.getBoolean("isOn");
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if(!converted) compound.setTag("tank", tank.writeToNBT(new NBTTagCompound())); else tankNew.writeToNBT(compound, "oil");
		compound.setBoolean("isOn", isOn);
		return super.writeToNBT(compound);
	}

	@Override
	public DrillType getDrillTier(){
		return DrillType.HITECH;
	}

	@Override
	public int getDrillRating(){
		return 100;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] {tankNew};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tankNew};
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
					new NTMFluidHandlerWrapper(null, this.getSendingTanks())
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
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineMiningLaser(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineMiningLaser(player.inventory, this);
	}
}