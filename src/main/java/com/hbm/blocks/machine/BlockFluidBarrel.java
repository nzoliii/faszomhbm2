package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BaseBarrel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.TileEntityBarrel;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class BlockFluidBarrel extends BlockContainer {

	private static final ThreadLocal<List<ItemStack>> HARVEST_DROPS = new ThreadLocal<>();
	private int capacity;
	public static boolean keepInventory;
	
	public BlockFluidBarrel(Material materialIn, int cap, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		capacity = cap;
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBarrel(capacity);
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
		if(this == ModBlocks.barrel_plastic) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "12,000"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannothot"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotcor"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
		}
		
		if(this == ModBlocks.barrel_corroded) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "6,000"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
			list.add(TextFormatting.RED + I18nUtil.resolveKey("desc.leaky"));
		}
		
		if(this == ModBlocks.barrel_iron) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "8,000"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotcor1"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
		}
		
		if(this == ModBlocks.barrel_steel) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "16,000"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.cancor"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannothighcor"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
		}
		
		if(this == ModBlocks.barrel_antimatter) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "16,000"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canam"));
		}
		
		if(this == ModBlocks.barrel_tcalloy) {
			list.add(TextFormatting.AQUA + I18nUtil.resolveKey("desc.capacity", "24,000"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhot"));
			list.add(TextFormatting.GREEN + I18nUtil.resolveKey("desc.canhighcor"));
			list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("desc.cannotam"));
		}
	}
	
	@Override
	public Block setSoundType(SoundType sound) {
		return super.setSoundType(sound);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
			
		} else if(!player.isSneaking()) {
			player.openGui(MainRegistry.instance, ModBlocks.guiID_barrel, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
			
		} else if(player.isSneaking()){
			TileEntityBarrel mileEntity = (TileEntityBarrel) world.getTileEntity(pos);

			if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
				FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand));

				mileEntity.tankNew.setTankType(type);
				mileEntity.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ")
								.setStyle(new Style().setColor(TextFormatting.YELLOW))
						.appendSibling(new TextComponentTranslation(type.getConditionalName()))
						.appendSibling(new TextComponentString("!")));
			}
			return true;

		} else {
			return false;
		}
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if(!keepInventory)
			InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
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
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, player, stack);
		IPersistentNBT.restoreData(world, pos, stack);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return BaseBarrel.BARREL_BB;
	}
}
