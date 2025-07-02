package com.hbm.tileentity.bomb;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluid.IFluidStandardReceiver;
import api.hbm.item.IDesignatorItem;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.missile.EntityMissileAntiBallistic;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileStealth;
import com.hbm.entity.missile.EntityMissileTier0.*;
import com.hbm.entity.missile.EntityMissileTier1.*;
import com.hbm.entity.missile.EntityMissileTier2.*;
import com.hbm.entity.missile.EntityMissileTier3.*;
import com.hbm.entity.missile.EntityMissileTier4.*;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IBomb.BombReturnCode;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerLaunchPadLarge;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUILaunchPadLarge;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemMissileStandard;
import com.hbm.items.weapon.ItemMissileStandard.MissileFuel;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IRadarCommandReceiver;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.TrackerUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public abstract class TileEntityLaunchPadBase extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IRadarCommandReceiver, SimpleComponent, CompatHandler.OCComponent, IFluidCopiable {
	
	/** Automatic instantiation of generic missiles, i.e. everything that both extends EntityMissileBaseNT and needs a designator */
	public static final HashMap<ComparableStack, Class<? extends EntityMissileBaseNT>> missiles = new HashMap();
	
	public static void registerLaunchables() {

		//Tier 0
		missiles.put(new ComparableStack(ModItems.missile_micro), EntityMissileMicro.class);
		missiles.put(new ComparableStack(ModItems.missile_schrabidium), EntityMissileSchrabidium.class);
		missiles.put(new ComparableStack(ModItems.missile_bhole), EntityMissileBHole.class);
		missiles.put(new ComparableStack(ModItems.missile_taint), EntityMissileTaint.class);
		missiles.put(new ComparableStack(ModItems.missile_emp), EntityMissileEMP.class);
		//Tier 1
		missiles.put(new ComparableStack(ModItems.missile_generic), EntityMissileGeneric.class);
		missiles.put(new ComparableStack(ModItems.missile_decoy), EntityMissileDecoy.class);
		missiles.put(new ComparableStack(ModItems.missile_incendiary), EntityMissileIncendiary.class);
		missiles.put(new ComparableStack(ModItems.missile_cluster), EntityMissileCluster.class);
		missiles.put(new ComparableStack(ModItems.missile_buster), EntityMissileBunkerBuster.class);
		//Tier 2
		missiles.put(new ComparableStack(ModItems.missile_strong), EntityMissileStrong.class);
		missiles.put(new ComparableStack(ModItems.missile_incendiary_strong), EntityMissileIncendiaryStrong.class);
		missiles.put(new ComparableStack(ModItems.missile_cluster_strong), EntityMissileClusterStrong.class);
		missiles.put(new ComparableStack(ModItems.missile_buster_strong), EntityMissileBusterStrong.class);
		missiles.put(new ComparableStack(ModItems.missile_emp_strong), EntityMissileEMPStrong.class);
		//Tier 3
		missiles.put(new ComparableStack(ModItems.missile_burst), EntityMissileBurst.class);
		missiles.put(new ComparableStack(ModItems.missile_inferno), EntityMissileInferno.class);
		missiles.put(new ComparableStack(ModItems.missile_rain), EntityMissileRain.class);
		missiles.put(new ComparableStack(ModItems.missile_drill), EntityMissileDrill.class);
		missiles.put(new ComparableStack(ModItems.missile_endo), EntityMissileEndo.class);
		missiles.put(new ComparableStack(ModItems.missile_exo), EntityMissileExo.class);
		//missiles.put(new ComparableStack(ModItems.missile_shuttle), EntityMissileShuttle.class);
		//Tier 4
		missiles.put(new ComparableStack(ModItems.missile_nuclear), EntityMissileNuclear.class);
		missiles.put(new ComparableStack(ModItems.missile_nuclear_cluster), EntityMissileMirv.class);
		missiles.put(new ComparableStack(ModItems.missile_volcano), EntityMissileVolcano.class);
		missiles.put(new ComparableStack(ModItems.missile_doomsday), EntityMissileDoomsday.class);
		missiles.put(new ComparableStack(ModItems.missile_n2), EntityMissileN2.class);
		
		missiles.put(new ComparableStack(ModItems.missile_stealth), EntityMissileStealth.class);
	}

	public ItemStack toRender;
	
	public long power;
	public final long maxPower = 100_000;

	public int prevRedstonePower;
	public int redstonePower;
	public Set<BlockPos> activatedBlocks = new HashSet<>(4);
	
	public int state = 0;
	public static final int STATE_MISSING = 0;
	public static final int STATE_LOADING = 1;
	public static final int STATE_READY = 2;
	
	public FluidTankNTM[] tanks;

	public TileEntityLaunchPadBase() {
		super(7);
		this.tanks = new FluidTankNTM[2];
		this.tanks[0] = new FluidTankNTM(Fluids.NONE, 24_000);
		this.tanks[1] = new FluidTankNTM(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.launchPad";
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int side) {
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return new int[] { 0 };
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot == 0 && this.isMissileValid(stack);
	}
	
	public abstract DirPos[] getConPos();
	
	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			if(world.getTotalWorldTime() % 20 == 0) {
				for(DirPos pos : getConPos()) {
					this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
					if(tanks[1].getTankType() != Fluids.NONE) this.trySubscribe(tanks[1].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
				}
			}
			
			if(this.redstonePower > 0 && this.prevRedstonePower <= 0) {
				this.launchFromDesignator();
			}
			
			this.prevRedstonePower = this.redstonePower;
			
			this.power = Library.chargeTEFromItems(inventory, 2, power, maxPower);
			tanks[0].loadTank(3, 4, inventory);
			tanks[1].loadTank(5, 6, inventory);
			
			if(this.isMissileValid()) {
				if(inventory.getStackInSlot(0).getItem() instanceof ItemMissileStandard) {
					ItemMissileStandard missile = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
					setFuel(missile);
				}
			}

			this.networkPackNT(250);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		
		buf.writeLong(this.power);
		buf.writeInt(this.state);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		
		if(!inventory.getStackInSlot(0).isEmpty()) {
			buf.writeBoolean(true);
			buf.writeInt(Item.getIdFromItem(inventory.getStackInSlot(0).getItem()));
			buf.writeShort((short) inventory.getStackInSlot(0).getItemDamage());
		} else {
			buf.writeBoolean(false);
		}
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		
		this.power = buf.readLong();
		this.state = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		
		if(buf.readBoolean()) {
			this.toRender = new ItemStack(Item.getItemById(buf.readInt()), 1, buf.readShort());
		} else {
			this.toRender = null;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");

		this.redstonePower = nbt.getInteger("redstonePower");
		this.prevRedstonePower = nbt.getInteger("prevRedstonePower");
		NBTTagCompound activatedBlocks = nbt.getCompoundTag("activatedBlocks");
		this.activatedBlocks.clear();
		for(int i = 0; i < activatedBlocks.getKeySet().size() / 3; i++) {
			this.activatedBlocks.add(new BlockPos(activatedBlocks.getInteger("x" + i), activatedBlocks.getInteger("y" + i), activatedBlocks.getInteger("z" + i)));
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");

		nbt.setInteger("redstonePower", redstonePower);
		nbt.setInteger("prevRedstonePower", prevRedstonePower);
		NBTTagCompound activatedBlocks = new NBTTagCompound();
		int i = 0;
		for(BlockPos p : this.activatedBlocks) {
			activatedBlocks.setInteger("x" + i, p.getX());
			activatedBlocks.setInteger("y" + i, p.getY());
			activatedBlocks.setInteger("z" + i, p.getZ());
			i++;
		}
		nbt.setTag("activatedBlocks", activatedBlocks);
		return super.writeToNBT(nbt);
	}

	public void updateRedstonePower(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		boolean powered = world.getStrongPower(pos) > 0;
		boolean contained = activatedBlocks.contains(pos);
		if(!contained && powered){
			activatedBlocks.add(pos);
			if(redstonePower == -1){
				redstonePower = 0;
			}
			redstonePower++;
		} else if(contained && !powered){
			activatedBlocks.remove(pos);
			redstonePower--;
			if(redstonePower == 0){
				redstonePower = -1;
			}
		}
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public FluidTankNTM[] getAllTanks() { return this.tanks; }
	@Override public FluidTankNTM[] getReceivingTanks() { return this.tanks; }
	
	@Override public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UP && dir != ForgeDirection.DOWN;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerLaunchPadLarge(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUILaunchPadLarge(player.inventory, this);
	}
	
	@SuppressWarnings("incomplete-switch") //shut up
	public void setFuel(ItemMissileStandard missile) {
		switch(missile.fuel) {
		case ETHANOL_PEROXIDE:
			tanks[0].setTankType(Fluids.ETHANOL);
			tanks[1].setTankType(Fluids.PEROXIDE);
			break;
		case KEROSENE_PEROXIDE:
			tanks[0].setTankType(Fluids.KEROSENE);
			tanks[1].setTankType(Fluids.PEROXIDE);
			break;
		case KEROSENE_LOXY:
			tanks[0].setTankType(Fluids.KEROSENE);
			tanks[1].setTankType(Fluids.OXYGEN);
			break;
		case JETFUEL_LOXY:
			tanks[0].setTankType(Fluids.KEROSENE_REFORM);
			tanks[1].setTankType(Fluids.OXYGEN);
			break;
		}
	}
	
	/** Requires the missile slot to be non-null and he item to be compatible */
	public boolean isMissileValid() {
		return !inventory.getStackInSlot(0).isEmpty() && isMissileValid(inventory.getStackInSlot(0));
	}
	
	public boolean isMissileValid(ItemStack stack) {
		return stack.getItem() instanceof ItemMissileStandard && ((ItemMissileStandard) stack.getItem()).launchable;
	}
	
	public boolean hasFuel() {
		if(this.power < 75_000) return false;
		
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemMissileStandard) {
			ItemMissileStandard missile = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
			if(this.tanks[0].getFill() < missile.fuelCap) return false;
			if(this.tanks[1].getFill() < missile.fuelCap) return false;
			
			return true;
		}
		
		return false;
	}
	
	public Entity instantiateMissile(int targetX, int targetZ) {
		
		if(inventory.getStackInSlot(0).isEmpty()) return null;
		
		Class<? extends EntityMissileBaseNT> clazz = TileEntityLaunchPadBase.missiles.get(new ComparableStack(inventory.getStackInSlot(0)).makeSingular());
		
		if(clazz != null) {
			try {
				EntityMissileBaseNT missile = clazz.getConstructor(World.class, float.class, float.class, float.class, int.class, int.class).newInstance(world, pos.getX() + 0.5F, pos.getY() + (float) getLaunchOffset() /* Position arguments need to be floats, jackass */, pos.getZ() + 0.5F, targetX, targetZ);
				if(GeneralConfig.enableExtendedLogging) MainRegistry.logger.log(Level.INFO, "[MISSILE] Tried to launch missile at " + pos.getX() + " / " + pos.getY() + " / " + pos.getZ() + " to " + pos.getX() + " / " + pos.getZ() + "!");
				missile.getDataManager().set(missile.pr3, (byte) MathHelper.clamp(this.getBlockMetadata() - 10, 2, 5));
				return missile;
			} catch(Exception e) { }
		}

		if(inventory.getStackInSlot(0).getItem() == ModItems.missile_anti_ballistic) {
			EntityMissileAntiBallistic missile = new EntityMissileAntiBallistic(world);
			missile.posX = pos.getX() + 0.5D;
			missile.posY = pos.getY() + getLaunchOffset();
			missile.posZ = pos.getZ() + 0.5D;
			return missile;
		}
		
		return null;
	}
	
	public void finalizeLaunch(Entity missile) {
		world.spawnEntity(missile);
		TrackerUtil.setTrackingRange(world, missile, 500);
		world.playSound(null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, HBMSoundHandler.missileTakeoff, SoundCategory.BLOCKS,  2.0F, 1.0F);

		this.power -= 75_000;
		
		if(!inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() instanceof ItemMissileStandard) {
			ItemMissileStandard item = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
			tanks[0].setFill(tanks[0].getFill() - item.fuelCap);
			tanks[1].setFill(tanks[1].getFill() - item.fuelCap);
		}
		
		this.inventory.getStackInSlot(0).shrink(1);
	}
	
	public BombReturnCode launchFromDesignator() {
		if(!canLaunch()) return BombReturnCode.ERROR_MISSING_COMPONENT;
		
		boolean needsDesignator = needsDesignator(inventory.getStackInSlot(0).getItem());

		int targetX = 0;
		int targetZ = 0;
		
		if(!inventory.getStackInSlot(1).isEmpty() && inventory.getStackInSlot(1).getItem() instanceof IDesignatorItem) {
			IDesignatorItem designator = (IDesignatorItem) inventory.getStackInSlot(1).getItem();
			
			if(!designator.isReady(world, inventory.getStackInSlot(1), pos.getX(), pos.getY(), pos.getZ()) && needsDesignator) return BombReturnCode.ERROR_MISSING_COMPONENT;
			
			Vec3d coords = designator.getCoords(world, inventory.getStackInSlot(1), pos.getX(), pos.getY(), pos.getZ());
			targetX = (int) Math.floor(coords.x);
			targetZ = (int) Math.floor(coords.z);
			
		} else {
			if(needsDesignator) return BombReturnCode.ERROR_MISSING_COMPONENT;
		}
		
		return this.launchToCoordinate(targetX, targetZ);
	}
	
	public BombReturnCode launchToEntity(Entity entity) {
		if(!canLaunch()) return BombReturnCode.ERROR_MISSING_COMPONENT;
		
		Entity e = instantiateMissile((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ));
		if(e != null) {
			
			if(e instanceof EntityMissileAntiBallistic) {
				EntityMissileAntiBallistic abm = (EntityMissileAntiBallistic) e;
				abm.tracking = entity;
			}
			
			finalizeLaunch(e);
			return BombReturnCode.LAUNCHED;
		}
		return BombReturnCode.ERROR_MISSING_COMPONENT;
	}
	
	public BombReturnCode launchToCoordinate(int targetX, int targetZ) {
		if(!canLaunch()) return BombReturnCode.ERROR_MISSING_COMPONENT;
		
		Entity e = instantiateMissile(targetX, targetZ);
		if(e != null) {
			finalizeLaunch(e);
			return BombReturnCode.LAUNCHED;
		}
		return BombReturnCode.ERROR_MISSING_COMPONENT;
	}

	@Override
	public boolean sendCommandPosition(int x, int y, int z) {
		return this.launchToCoordinate(x, z) == BombReturnCode.LAUNCHED;
	}

	@Override
	public boolean sendCommandEntity(Entity target) {
		return this.launchToEntity(target) == BombReturnCode.LAUNCHED;
	}
	
	public boolean needsDesignator(Item item) {
		return item != ModItems.missile_anti_ballistic;
	}
	
	/** Full launch condition, checks if the item is launchable, fuel and power are present and any additional checks based on launch pad type */
	public boolean canLaunch() {
		return this.isMissileValid() && this.hasFuel() && this.isReadyForLaunch();
	}
	
	public int getFuelState() {
		return getGaugeState(0);
	}
	
	public int getOxidizerState() {
		return getGaugeState(1);
	}
	
	public int getGaugeState(int tank) {
		if(inventory.getStackInSlot(0).isEmpty()) return 0;
		
		if(inventory.getStackInSlot(0).getItem() instanceof ItemMissileStandard) {
			ItemMissileStandard missile = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
			MissileFuel fuel = missile.fuel;
			
			if(fuel == MissileFuel.SOLID) return 0;
			return tanks[tank].getFill() >= missile.fuelCap ? 1 : -1;
		}
		
		return 0;
	}
	
	/** Any extra conditions for launching in addition to the missile being valid and fueled */
	public abstract boolean isReadyForLaunch();
	public abstract double getLaunchOffset();

	// do some opencomputer stuff
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_launch_pad";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {getPower(), getMaxPower()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getFluid(Context context, Arguments args) {
		return new Object[] {
				this.tanks[0].getFill(), this.tanks[0].getMaxFill(), this.tanks[0].getTankType().getTranslationKey(),
				this.tanks[1].getFill(), this.tanks[1].getMaxFill(), this.tanks[1].getTankType().getTranslationKey()
		};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] canLaunch(Context context, Arguments args) {
		return new Object[] {canLaunch()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getTier(Context context, Arguments args) {
		if(!isMissileValid())
			return new Object[] {};
		ItemMissileStandard missile = (ItemMissileStandard) inventory.getStackInSlot(0).getItem();
		if(missile.tier == ItemMissileStandard.MissileTier.TIER0)
			return new Object[] {0};
		if(missile.tier == ItemMissileStandard.MissileTier.TIER1)
			return new Object[] {1};
		if(missile.tier == ItemMissileStandard.MissileTier.TIER2)
			return new Object[] {2};
		if(missile.tier == ItemMissileStandard.MissileTier.TIER3)
			return new Object[] {3};
		if(missile.tier == ItemMissileStandard.MissileTier.TIER4)
			return new Object[] {4};
		return new Object[] {5}; // unknown tier
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] launch(Context context, Arguments args) {
		if(canLaunch()) {
			return new Object[] {sendCommandPosition(args.checkInteger(0), -1 /*unused anyway*/, args.checkInteger(1))};
		}
		return new Object[] {false};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String[] methods() {
		return new String[] {
				"getEnergyInfo",
				"getFluid",
				"canLaunch",
				"getTier",
				"launch"
		};
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public Object[] invoke(String method, Context context, Arguments args) throws Exception {
		switch(method) {
			case ("getEnergyInfo"):
				return getEnergyInfo(context, args);
			case ("getFluid"):
				return getFluid(context, args);
			case ("canLaunch"):
				return canLaunch(context, args);
			case ("getTier"):
				return getTier(context, args);
			case ("launch"):
				return launch(context, args);
		}
	throw new NoSuchMethodException();
	}

	@Override
	public int[] getFluidIDToCopy() {
		return new int[]{tanks[0].getTankType().getID(), tanks[1].getTankType().getID()};
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
					new NTMFluidHandlerWrapper(this.getReceivingTanks(), null)
			);
		}
		return super.getCapability(capability, facing);
	}
}
