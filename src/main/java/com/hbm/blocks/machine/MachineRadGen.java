package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public class MachineRadGen extends BlockDummyable {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public MachineRadGen(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {

		if(meta >= 12)
			return new TileEntityMachineRadGen();

		if(meta >= 6)
			return new TileEntityProxyCombo(true, true, false);

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 3, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
		this.makeExtra(world, x + dir.offsetX * -5, y, z + dir.offsetZ * -5);
		this.makeExtra(world, x + dir.offsetX * o + rot.offsetX, y, z + dir.offsetZ * o + rot.offsetZ);
		this.makeExtra(world, x + dir.offsetX * o - rot.offsetX, y, z + dir.offsetZ * o - rot.offsetZ);
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

			if(posC == null)
				return false;

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, posC[0], posC[1], posC[2]);
			return true;
		} else {
			return false;
		}
	}

}
