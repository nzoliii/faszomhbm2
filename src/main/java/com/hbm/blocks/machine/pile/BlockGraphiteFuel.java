package com.hbm.blocks.machine.pile;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.pile.TileEntityPileFuel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockGraphiteFuel extends BlockGraphiteDrilledTE implements IToolable {

	public BlockGraphiteFuel(String s){
		super(s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityPileFuel pile = new TileEntityPileFuel();
		if((meta & 8) != 0)
			pile.progress = pile.maxProgress - 1000; // pu239 rods cringe :(

		return pile;
	}
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		if(!world.isRemote) {
			BlockPos pos = new BlockPos(x, y, z);
			int meta = getMetaFromState(world.getBlockState(pos));
			if(tool == ToolType.SCREWDRIVER) {

				int cfg = meta & 3;

				if(side.getIndex() == cfg * 2 || side.getIndex() == cfg * 2 + 1) {
					world.setBlockState(pos, ModBlocks.block_graphite_drilled.getDefaultState().withProperty(BlockMeta.META, meta & 7), 3);
					ejectItem(world, x, y, z, side, getInsertedItem(meta));
				}
			}
			
			if(tool == ToolType.HAND_DRILL) {
				TileEntityPileFuel pile = (TileEntityPileFuel) world.getTileEntity(pos);
				player.sendMessage(new TextComponentString("CP1 FUEL ASSEMBLY " + x + " " + y + " " + z).setStyle(new Style().setColor(TextFormatting.GOLD)));
				player.sendMessage(new TextComponentString("HEAT: " + pile.heat + "/" + pile.maxHeat).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				player.sendMessage(new TextComponentString("DEPLETION: " + pile.progress + "/" + pile.maxProgress).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				player.sendMessage(new TextComponentString("FLUX: " + pile.lastNeutrons).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				if((meta & 8) == 8)
					player.sendMessage(new TextComponentString("PU-239 RICH").setStyle(new Style().setColor(TextFormatting.DARK_GREEN)));
			}
		}
		
		return true;
	}

	@Override
	protected ItemStack getInsertedItem(int meta) {
		return (meta & 8) == 8 ? new ItemStack(ModItems.pile_rod_pu239) : new ItemStack(ModItems.pile_rod_uranium);
	}
}