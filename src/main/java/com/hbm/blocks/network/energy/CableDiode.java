package com.hbm.blocks.network.energy;

import com.hbm.api.block.IToolable;
import com.hbm.api.energymk2.IEnergyConnectorBlock;
import com.hbm.api.energymk2.IEnergyConnectorMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.INBTPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CableDiode extends BlockContainer implements IEnergyConnectorBlock, ILookOverlay, IToolable, ITooltipProvider {

	public static final PropertyDirection FACING = BlockDirectional.FACING;

	public CableDiode(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);
		return this.getDefaultState().withProperty(FACING, enumfacing);
	}

	@Override
	public @NotNull IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@NotNull
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, @NotNull EntityLivingBase placer, @NotNull ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)));
	}

	@Override
	public boolean canConnect(IBlockAccess world, BlockPos pos, ForgeDirection dir) {
		return true;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){

		TileEntityDiode te = (TileEntityDiode)world.getTileEntity(new BlockPos(x, y, z));

		if(world.isRemote || te == null)
			return true;

		if(tool == ToolType.SCREWDRIVER) {
			if(te.level < 17)
				te.level++;
			te.markDirty();
			INBTPacketReceiver.networkPack(te, te.packValues(), 20);
			return true;
		}

		if(tool == ToolType.HAND_DRILL) {
			if(te.level > 1)
				te.level--;
			te.markDirty();
			INBTPacketReceiver.networkPack(te, te.packValues(), 20);
			return true;
		}

		if(tool == ToolType.DEFUSER) {
			int p = te.priority.ordinal() + 1;
			if(p >= ConnectionPriority.values().length) p = 0;
			te.priority = ConnectionPriority.values()[p];
			te.markDirty();
			INBTPacketReceiver.networkPack(te, te.packValues(), 20);
			return true;
		}

		return false;
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		this.addStandardInfo(list);
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityDiode diode))
			return;

        List<String> text = new ArrayList<>();
		text.add("Max.: " + Library.getShortNumber(diode.getMaxPower()*20) + "HE/s");
		text.add("Priority: " + diode.priority.name());

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityDiode();
	}

	@Override
	public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public static class TileEntityDiode extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2, INBTPacketReceiver {

		@Override
		public void networkUnpack(NBTTagCompound nbt){
			level = nbt.getInteger("level");
			priority = ConnectionPriority.values()[nbt.getByte("p")];
		}

		public NBTTagCompound packValues(){
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("level", level);
			nbt.setByte("p", (byte) this.priority.ordinal());
			return nbt;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			level = nbt.getInteger("level");
			priority = ConnectionPriority.values()[nbt.getByte("p")];
		}

		@Override
		public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("level", level);
			nbt.setByte("p", (byte) this.priority.ordinal());
			return nbt;
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket(){
			return new SPacketUpdateTileEntity(this.getPos(), 0, this.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public @NotNull NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}

		@Override
		public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		int level = 1;

		private ForgeDirection getDir() {
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof CableDiode) {
				return ForgeDirection.getOrientation(state.getBlock().getMetaFromState(state)).getOpposite();
			}
			return ForgeDirection.UNKNOWN;
		}

		@Override
		public void update() {
			if (!world.isRemote) {
				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {

					if (dir == getDir())
						continue;

					this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
				}
			}
		}

		@Override
		public boolean canConnect(ForgeDirection dir) {
			return dir != getDir();
		}

		/** Used as an intra-tick tracker for how much energy has been transmitted, resets to 0 each tick and maxes out based on transfer */
		private long power;
		private boolean recursionBrake = false;
		private int pulses = 0;
		public ConnectionPriority priority = ConnectionPriority.NORMAL;

		@Override
		public long transferPower(long power, boolean simulate) {
			if(recursionBrake || power <= 0)
				return power;
			if(this.getPower() >= this.getMaxPower())
				return power;
			if (!simulate) {
				pulses++;
				if(pulses > 10) return power;
			}

			recursionBrake = true;

			long transferredInThisCall = 0;
			ForgeDirection dir = getDir();
			TileEntity te = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
			long remainingTickCapacity = this.getMaxPower() - this.getPower();
			Nodespace.PowerNode node = Nodespace.getNode(world, pos);
			if(node != null && !node.expired && node.hasValidNet() && te instanceof IEnergyConnectorMK2 && ((IEnergyConnectorMK2) te).canConnect(dir.getOpposite())) {
				long toTransfer = Math.min(power, this.getReceiverSpeed());
				toTransfer = Math.min(toTransfer, remainingTickCapacity);

				if (toTransfer > 0) {
					long remainder = node.net.sendPowerDiode(toTransfer, simulate);
					transferredInThisCall = toTransfer - remainder;
				}

			} else if(te instanceof IEnergyReceiverMK2 rec && te != this) {
                if(rec.canConnect(dir.getOpposite())) {
					long toTransfer = Math.min(power, rec.getReceiverSpeed());
					toTransfer = Math.min(toTransfer, remainingTickCapacity);

					if (toTransfer > 0) {
						long remainder = rec.transferPower(toTransfer, simulate);
						transferredInThisCall = toTransfer - remainder;
					}
				}
			}
			if (!simulate && transferredInThisCall > 0) {
				this.power += transferredInThisCall;
			}
			recursionBrake = false;
			return power - transferredInThisCall;
		}


		@Override
		public long getMaxPower() {
			return (long) Math.pow(10, level) >> 1;
		}

		@Override
		public long getPower() {
			return Math.min(power, this.getMaxPower());
		}

		@Override
		public void setPower(long power) {
			this.power = power;
			if(this.power == 0) {
				this.pulses = 0;
			}
		}

		@Override
		public ConnectionPriority getPriority() {
			return this.priority;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY) {
				return true;
			}
			return super.hasCapability(capability, facing);
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			if (capability == CapabilityEnergy.ENERGY) {
				return CapabilityEnergy.ENERGY.cast(
						new NTMEnergyCapabilityWrapper(this)
				);
			}
			return super.getCapability(capability, facing);
		}
	}
}