package com.hbm.blocks.turret;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretHIMARS;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TurretHIMARS extends BlockDummyable {
    public TurretHIMARS(Material materialIn, String s){
        super(materialIn, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12)
            return new TileEntityTurretHIMARS();
        return new TileEntityProxyCombo(true, true, false);
    }

    @Override
    public int[] getDimensions() {
        return new int[] { 1, 0, 2, 1, 2, 1 };
    }

    public int getOffset() {
        return 1;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
        return super.standardOpenBehavior(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, 0);
    }
}