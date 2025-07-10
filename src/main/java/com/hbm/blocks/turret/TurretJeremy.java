package com.hbm.blocks.turret;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretJeremy;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TurretJeremy extends TurretBaseNT {

	public TurretJeremy(Material materialIn, String s){
		super(materialIn, s);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12)
			return new TileEntityTurretJeremy();
		return new TileEntityProxyCombo(true, true, false);
	}

}
