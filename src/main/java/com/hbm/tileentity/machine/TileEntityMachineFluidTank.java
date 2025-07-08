package com.hbm.tileentity.machine;

import api.hbm.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmCapability;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerMachineFluidTank;
import com.hbm.inventory.control_panel.*;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.*;
import com.hbm.inventory.gui.GUIMachineFluidTank;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.tileentity.*;
import com.hbm.util.ParticleUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityMachineFluidTank extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiver, IPersistentNBT, IControllable, IGUIProvider, IOverpressurable, IRepairable, IFFtoNTMF, IFluidCopiable {

	public FluidTankNTM tankNew;
	public FluidTank tank;
	public Fluid oldFluid;
	/** 0 = receive-only, 1 = both, 2 = send-only, 3 = disabled */
	public short mode = 0;
	public static final short modes = 4;
	public boolean hasExploded = false;
	protected boolean sendingBrake = false;
	public boolean onFire = false;
	public int age = 0;
	public static int[] slots = { 2 };
	public byte lastRedstone = 0;
	public Explosion lastExplosion = null;

	private static boolean converted = false;

	public TileEntityMachineFluidTank() {
		super(6);
		tank = new FluidTank(256000);
		tankNew = new FluidTankNTM(Fluids.NONE, 256000);

		converted = true;
	}

	public String getName() {
		return "container.fluidtank";
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(!converted) {
			tank.readFromNBT(compound);
			oldFluid = tank.getFluid() != null ? tank.getFluid().getFluid() :Fluids.NONE.getFF();;
		} else tankNew.readFromNBT(compound, "tank");
		mode = compound.getShort("mode");
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if(!converted) tank.writeToNBT(compound); else tankNew.writeToNBT(compound, "tank");
		compound.setShort("mode", mode);
		return super.writeToNBT(compound);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return slots;
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
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(tankNew) {
				@Override
				public int fill(FluidStack resource, boolean doFill) {
					if (mode == 0 || mode == 1) {
						return super.fill(resource, doFill);
					}
					return 0;
				}

				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {
					if (mode == 2 || mode == 1) {
						return super.drain(resource, doDrain);
					}
					return null;
				}

				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {
					if (mode == 2 || mode == 1) {
						return super.drain(maxDrain, doDrain);
					}
					return null;
				}
			});
		}
		return super.getCapability(capability, facing);
	}

	public byte getComparatorPower() {
		if(tankNew.getFill() == 0) return 0;
		double frac = (double) tankNew.getFill() / (double) tankNew.getMaxFill() * 15D;
		return (byte) (MathHelper.clamp((int) frac + 1, 0, 15));
	}

	@Override
	public void update() {
		if (!world.isRemote) {
			if(!converted){
				convertAndSetFluid(oldFluid, tank, tankNew);
				converted = true;
			}

			//meta below 12 means that it's an old multiblock configuration
			//thanks for actually doing my work, Bob
			if(this.getBlockMetadata() < 12) {
				//get old direction
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata()).getRotation(ForgeDirection.DOWN);
				//remove tile from the world to prevent inventory dropping
				world.removeTileEntity(pos);
				//use fillspace to create a new multiblock configuration
				world.setBlockState(pos, ModBlocks.machine_fluidtank.getStateFromMeta(dir.ordinal() + 10), 3);
				MultiblockHandlerXR.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), ((BlockDummyable) ModBlocks.machine_fluidtank).getDimensions(), ModBlocks.machine_fluidtank, dir);
				//load the tile data to restore the old values
				NBTTagCompound data = new NBTTagCompound();
				this.writeToNBT(data);
				world.getTileEntity(pos).readFromNBT(data);
				return;
			}

			if(!hasExploded) {
				age++;

				if(age >= 20) {
					age = 0;
					this.markDirty();
				}

				this.sendingBrake = true;
				tankNew.setFill(TileEntityBarrel.transmitFluidFairly(world, tankNew, this, tankNew.getFill(), this.mode == 0 || this.mode == 1, this.mode == 1 || this.mode == 2, getConPos()));
				this.sendingBrake = false;

				tankNew.loadTank(2, 3, inventory);
				tankNew.setType(0, 1, inventory);
			} else {
				for(DirPos pos : getConPos()) this.tryUnsubscribe(tankNew.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ());
			}

			byte comp = this.getComparatorPower(); //comparator shit
			if(comp != this.lastRedstone) {
				this.markDirty();
				for(DirPos pos : getConPos()) this.updateRedstoneConnection(pos);
			}
			this.lastRedstone = comp;

			if(tankNew.getFill() > 0) {
				if(tankNew.getTankType().isAntimatter()) {
					new ExplosionVNT(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 5F).makeAmat().setBlockAllocator(null).setBlockProcessor(null).explode();
					this.explode();
					this.tankNew.setFill(0);
				}

				if(tankNew.getTankType().hasTrait(FT_Corrosive.class) && tankNew.getTankType().getTrait(FT_Corrosive.class).isHighlyCorrosive()) {
					this.explode();
				}

				if(this.hasExploded) {

					int leaking = 0;
					if(tankNew.getTankType().isAntimatter()) {
						leaking = tankNew.getFill();
					} else if(tankNew.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous.class) || tankNew.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous_ART.class)) {
						leaking = Math.min(tankNew.getFill(), tankNew.getMaxFill() / 100);
					} else {
						leaking = Math.min(tankNew.getFill(), tankNew.getMaxFill() / 10000);
					}

					updateLeak(leaking);
				}
			}

			tankNew.unloadTank(4, 5, inventory);

			this.networkPackNT(150);
		}

		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos, pos.add(1, 2.875, 1)).offset(dir.offsetX * 0.5 - rot.offsetX * 2.25, 0, dir.offsetZ * 0.5 - rot.offsetZ * 2.25));

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
		buf.writeShort(mode);
		buf.writeBoolean(hasExploded);
		tankNew.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		mode = buf.readShort();
		hasExploded = buf.readBoolean();
		tankNew.deserialize(buf);
	}

	/** called when the tank breaks due to hazardous materials or external force, can be used to quickly void part of the tank or spawn a mushroom cloud */
	public void explode() {
		this.hasExploded = true;
		this.onFire = tankNew.getTankType().hasTrait(FT_Flammable.class);
		this.markDirty();
	}

	@Override
	public void explode(World world, int x, int y, int z) {

		if(this.hasExploded) return;
		this.onFire = tankNew.getTankType().hasTrait(FT_Flammable.class);
		this.hasExploded = true;
		this.markDirty();
	}

	/** called every tick post explosion, used for leaking fluid and spawning particles */
	public void updateLeak(int amount) {
		if(!hasExploded) return;
		if(amount <= 0) return;

		this.tankNew.getTankType().onFluidRelease(this, tankNew, amount);
		this.tankNew.setFill(Math.max(0, this.tankNew.getFill() - amount));

		FluidType type = tankNew.getTankType();

		if(type.hasTrait(FluidTraitSimple.FT_Amat.class)) {
			new ExplosionVNT(world, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 5F).makeAmat().setBlockAllocator(null).setBlockProcessor(null).explode();

		} else if(type.hasTrait(FT_Flammable.class) && onFire) {
			List<Entity> affected = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() - 1.5, pos.getY(), pos.getZ() - 1.5, pos.getX() + 2.5, pos.getY() + 5, pos.getZ() + 2.5));
			for(Entity e : affected) e.setFire(5);
			Random rand = world.rand;
			ParticleUtil.spawnGasFlame(world, pos.getX() + rand.nextDouble(), pos.getY() + 0.5 + rand.nextDouble(), pos.getZ() + rand.nextDouble(), rand.nextGaussian() * 0.2, 0.1, rand.nextGaussian() * 0.2);

			if(world.getTotalWorldTime() % 5 == 0) {
				FT_Polluting.pollute(world, pos.getX(), pos.getY(), pos.getZ(), tankNew.getTankType(), FluidTrait.FluidReleaseType.BURN, amount * 5);
			}

		} else if(type.hasTrait(FluidTraitSimple.FT_Gaseous.class) || type.hasTrait(FluidTraitSimple.FT_Gaseous_ART.class)) {

			if(world.getTotalWorldTime() % 5 == 0) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "tower");
				data.setFloat("lift", 1F);
				data.setFloat("base", 1F);
				data.setFloat("max", 5F);
				data.setInteger("life", 100 + world.rand.nextInt(20));
				data.setInteger("color", tankNew.getTankType().getColor());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
			}

			if(world.getTotalWorldTime() % 5 == 0 ) {
				FT_Polluting.pollute(world, pos.getX(), pos.getY(), pos.getZ(), tankNew.getTankType(), FluidTrait.FluidReleaseType.SPILL, amount * 5);
			}
		}
	}

	@Override
	public void tryExtinguish(World world, int x, int y, int z, EnumExtinguishType type) {
		if(!this.hasExploded || !this.onFire) return;

		if(type == EnumExtinguishType.WATER) {
			if(tankNew.getTankType().hasTrait(FluidTraitSimple.FT_Liquid.class)) { // extinguishing oil with water is a terrible idea!
				world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 5F, true, true);
			} else {
				this.onFire = false;
				this.markDirty();
				return;
			}
		}

		if(type == EnumExtinguishType.FOAM || type == EnumExtinguishType.CO2) {
			this.onFire = false;
			this.markDirty();
		}
	}

	protected DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() - 1, Library.POS_X),
				new DirPos(pos.getX() + 2, pos.getY(), pos.getZ() + 1, Library.POS_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() - 1, Library.NEG_X),
				new DirPos(pos.getX() - 2, pos.getY(), pos.getZ() + 1, Library.NEG_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() + 2, Library.POS_Z),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ() - 2, Library.NEG_Z)
		};
	}

	@Override
	public void handleButtonPacket(int value, int meta) {
		mode = (short) ((mode + 1) % modes);
		if (!world.isRemote) {
			broadcastControlEvt();
		}
		markDirty();
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
	public long transferFluid(FluidType type, int pressure, long fluid) {
		long toTransfer = Math.min(getDemand(type, pressure), fluid);
		tankNew.setFill(tankNew.getFill() + (int) toTransfer);
		return fluid - toTransfer;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {

		if(this.mode == 2 || this.mode == 3 || this.sendingBrake)
			return 0;

		if(tankNew.getPressure() != pressure) return 0;

		return type == tankNew.getTankType() ? tankNew.getMaxFill() - tankNew.getFill() : 0;
	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		if(tankNew.getFill() == 0 && !this.hasExploded) return;
		NBTTagCompound data = new NBTTagCompound();
		this.tankNew.writeToNBT(data, "tank");
		data.setShort("mode", mode);
		data.setBoolean("hasExploded", hasExploded);
		data.setBoolean("onFire", onFire);
		nbt.setTag(NBT_PERSISTENT_KEY, data);
	}

	@Override
	public void readNBT(NBTTagCompound nbt) {
		NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
		this.tankNew.readFromNBT(data, "tank");
		this.mode = data.getShort("mode");
		this.hasExploded = data.getBoolean("hasExploded");
		this.onFire = data.getBoolean("onFire");
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] { tankNew };
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		if(this.hasExploded) return new FluidTankNTM[0];
		return (mode == 1 || mode == 2) ? new FluidTankNTM[] {tankNew} : new FluidTankNTM[0];
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		if(this.hasExploded || this.sendingBrake) return new FluidTankNTM[0];
		return (mode == 0 || mode == 1) ? new FluidTankNTM[] {tankNew} : new FluidTankNTM[0];
	}

	@Override
	public int[] getFluidIDToCopy() {
		return new int[] {tankNew.getTankType().getID()};
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return tankNew;
	}

	// control panel

	@Override
	public Map<String, DataValue> getQueryData() {
		Map<String, DataValue> data = new HashMap<>();

		if (tankNew.getTankType() != Fluids.NONE) {
			data.put("t0_fluidType", new DataValueString(tankNew.getTankType().getLocalizedName()));
		}
		data.put("t0_fluidAmount", new DataValueFloat(tankNew.getFill()));
		data.put("mode", new DataValueFloat(mode));

		return data;
	}

	@Override
	public void receiveEvent(BlockPos from, ControlEvent e) {
		if (e.name.equals("tank_set_mode")) {
			mode = (short) (e.vars.get("mode").getNumber() % modes);
			broadcastControlEvt();
		}
	}

	public void broadcastControlEvt() {
		ControlEventSystem.get(world).broadcastToSubscribed(this, ControlEvent.newEvent("tank_set_mode").setVar("mode", new DataValueFloat(mode)));
	}

	@Override
	public List<String> getInEvents() {
		return Collections.singletonList("tank_set_mode");
	}

	@Override
	public List<String> getOutEvents() {
		return Collections.singletonList("tank_set_mode");
	}

	@Override
	public void validate() {
		super.validate();
		ControlEventSystem.get(world).addControllable(this);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		ControlEventSystem.get(world).removeControllable(this);
	}

	@Override
	public BlockPos getControlPos() {
		return getPos();
	}

	@Override
	public World getControlWorld() {
		return getWorld();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineFluidTank(player.inventory, (TileEntityMachineFluidTank) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineFluidTank(player.inventory, (TileEntityMachineFluidTank) world.getTileEntity(new BlockPos(x, y, z)));
	}

	@Override
	public boolean isDamaged() {
		return this.hasExploded;
	}

	List<RecipesCommon.AStack> repair = new ArrayList<>();
	@Override
	public List<RecipesCommon.AStack> getRepairMaterials() {

		if(!repair.isEmpty())
			return repair;

		repair.add(new RecipesCommon.OreDictStack(OreDictManager.STEEL.plate(), 6));
		return repair;
	}

	@Override
	public void repair() {
		this.hasExploded = false;
		this.markDirty();
	}

}