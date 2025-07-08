package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemForgeFluidIdentifier extends Item implements IItemFluidIdentifier {

	public static final ModelResourceLocation identifierModel = new ModelResourceLocation(RefStrings.MODID + ":forge_fluid_identifier", "inventory");

	public ItemForgeFluidIdentifier(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.partsTab);

		ModItems.ALL_ITEMS.add(this);
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack) {
		return itemStack.copy();
	}

	@Override
	public boolean hasContainerItem() {
		return true;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			FluidType[] order = Fluids.getInNiceOrder();
			for (int i = 1; i < order.length; ++i) {
				if (!order[i].hasNoID()) {
					items.add(new ItemStack(this, 1, order[i].getID()));
				}
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		if (!(stack.getItem() instanceof ItemForgeFluidIdentifier))
			return;
		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("info.templatefolder"));
		list.add("");
		list.add(I18nUtil.resolveKey("desc.unfluidid"));
		if (Fluids.fromID(stack.getItemDamage()).getLocalizedName() != null)
			list.add("   " + Fluids.fromID(stack.getItemDamage()).getLocalizedName());
		else
			list.add("   " + "ERROR - bad data");
	}
	@Override
	public FluidType getType(World world, int x, int y, int z, ItemStack stack) {
		return Fluids.fromID(stack.getItemDamage());
	}

	public static void spreadType(World worldIn, BlockPos pos, FluidType hand, FluidType pipe, int x){
		if(x > 0){
			TileEntity te = worldIn.getTileEntity(pos);
			if(te != null && te instanceof TileEntityPipeBaseNT){
				TileEntityPipeBaseNT duct = (TileEntityPipeBaseNT) te;
				if(duct.getType() == pipe){
					duct.setType(hand);
					duct.markDirty();
					spreadType(worldIn, pos.add(1, 0, 0), hand, pipe, x-1);
					spreadType(worldIn, pos.add(0, 1, 0), hand, pipe, x-1);
					spreadType(worldIn, pos.add(0, 0, 1), hand, pipe, x-1);
					spreadType(worldIn, pos.add(-1, 0, 0), hand, pipe, x-1);
					spreadType(worldIn, pos.add(0, -1, 0), hand, pipe, x-1);
					spreadType(worldIn, pos.add(0, 0, -1), hand, pipe, x-1);
				}
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		TileEntity te = worldIn.getTileEntity(pos);
		TileEntityPipeBaseNT duct = null;
		if(te != null && te instanceof TileEntityPipeBaseNT){
			duct = (TileEntityPipeBaseNT) te;
		}
		if(duct != null){
			if(player.isSneaking()){
				if(Fluids.NONE != duct.getType()){
					spreadType(worldIn, pos, Fluids.NONE, duct.getType(), 256);
				}
			}else{
				if(getType(worldIn, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand)) != duct.getType()){
					spreadType(worldIn, pos, getType(worldIn, pos.getX(), pos.getY(), pos.getZ(),player.getHeldItem(hand)), duct.getType(), 256);
				}
			}
		}
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}
}
