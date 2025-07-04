package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityBombletZeta;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MachineFluidTank extends BlockDummyable implements IPersistentInfoProvider, IToolable, ILookOverlay {
	private static final ThreadLocal<List<ItemStack>> HARVEST_DROPS = new ThreadLocal<>();
	public MachineFluidTank(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineFluidTank();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(ModBlocks.machine_fluidtank);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		}

		int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
		if(posC == null)
			return false;

		if(!player.isSneaking()) {
			TileEntityMachineFluidTank entity = (TileEntityMachineFluidTank) world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

			if(entity != null) {
				if(entity.hasExploded) return false;
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, posC[0], posC[1], posC[2]);
			}
			return true;
		} else if(player.isSneaking()){

			TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));

			if(tank != null) {
				if(tank.hasExploded) return false;
				if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
					FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, posC[0], posC[1], posC[2], player.getHeldItem(hand));

					tank.tankNew.setTankType(type);
					tank.markDirty();
					player.sendMessage(new TextComponentString("Changed type to ")
							.setStyle(new Style().setColor(TextFormatting.YELLOW))
							.appendSibling(new TextComponentTranslation(type.getConditionalName()))
							.appendSibling(new TextComponentString("!")));
				}
			}
			return true;
		}else {
			return false;
		}
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ + 1);
		this.makeExtra(world, x - dir.offsetX + 1, y, z - dir.offsetZ - 1);
		this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ + 1);
		this.makeExtra(world, x - dir.offsetX - 1, y, z - dir.offsetZ - 1);
	}

	@Override
	public boolean removedByPlayer(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull EntityPlayer player, boolean willHarvest) {
		if (willHarvest) {
			ArrayList<ItemStack> drops = IPersistentNBT.getDrops(world, pos, this);
			HARVEST_DROPS.set(drops);
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@NotNull
	@Override
	public List<ItemStack> getDrops(@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull IBlockState state, int fortune) {
		List<ItemStack> drops = HARVEST_DROPS.get();
		HARVEST_DROPS.remove();
		return drops == null ? new ArrayList<>() : (ArrayList<ItemStack>) drops;
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, 0);
		tank.readFromNBT(persistentTag, "tank");
		list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {

		int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
		if(posC == null) return;
		TileEntity core = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
		if(!(core instanceof TileEntityMachineFluidTank)) return;

		TileEntityMachineFluidTank tank = (TileEntityMachineFluidTank) core;
		if(tank.lastExplosion == explosion) return;
		tank.lastExplosion = explosion;

		if(!tank.hasExploded) {
			tank.explode();
			Entity exploder = explosion.exploder;
			if(exploder instanceof EntityBombletZeta) {
				if(tank.tankNew.getTankType().getTrait(FT_Flammable.class) == null) return;

				List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
						new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).expand(100, 100, 100));

				for(EntityPlayer p : players) AdvancementManager.grantAchievement(p, AdvancementManager.achInferno);
			}
		} else {
			world.setBlockToAir(new BlockPos(posC[0], posC[1], posC[2]));
		}
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {

		if(tool != ToolType.TORCH) return false;
		return IRepairable.tryRepairMultiblock(world, x, y, z, this, player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		IRepairable.addGenericOverlay(event, world, x, y, z, this);
	}
	
}
