package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineGasFlare extends BlockDummyable implements ITooltipProvider {

	public MachineGasFlare(Material materialIn, String s) {
		super(materialIn, s);
		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, 1.5D, 3.875D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-0.75D, 3.875D, -0.75D, 0.75D, 9, 0.75D));
		this.bounding.add(new AxisAlignedBB(-1.5D, 9D, -1.5D, 1.5D, 9.375D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-0.75D, 9.375D, -0.75D, 0.75D, 12, 0.75D));
		this.FULL_BLOCK_AABB.setMaxY(0.999D); //item bounce prevention
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {

		if(meta >= 12) return new TileEntityMachineGasFlare();
		if(meta >= 6) return new TileEntityProxyCombo(false, true, true);
		return null;
	}

	@Override
	public boolean onBlockActivated(@NotNull World world, BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {11, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 1);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World player, List<String> list, @NotNull ITooltipFlag advanced) {

		list.add(TextFormatting.GOLD + "Can burn fluids and vent gasses");
		list.add(TextFormatting.GOLD + "Burns up to " + TextFormatting.RED + "10mB/t");
		list.add(TextFormatting.GOLD + "Vents up to " + TextFormatting.RED + "50mB/t");
		list.add("");
		list.add(TextFormatting.YELLOW + "Fuel efficiency:");
		list.add(TextFormatting.YELLOW + "-Flammable Gasses: " + TextFormatting.RED + "20%");
		list.add(TextFormatting.YELLOW + "-Flammable Liquids: " + TextFormatting.RED + "10%");
	}

}
