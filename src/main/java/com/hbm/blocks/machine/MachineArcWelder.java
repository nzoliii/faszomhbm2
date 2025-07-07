package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineArcWelder;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class MachineArcWelder extends BlockDummyable {

  public MachineArcWelder(Material material, String s) {
    super(material, s);
  }

  @Override
  public TileEntity createNewTileEntity(@NotNull World world, int meta) {
    if (meta >= 12) return new TileEntityMachineArcWelder();
    return new TileEntityProxyCombo(true, true, true);
  }

  @Override
  public boolean onBlockActivated(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      @NotNull EntityPlayer player,
      @NotNull EnumHand hand,
      @NotNull EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);
  }

  @Override
  public int[] getDimensions() {
    return new int[] {1, 0, 1, 0, 1, 1};
  }

  @Override
  public int getOffset() {
    return 0;
  }
}
