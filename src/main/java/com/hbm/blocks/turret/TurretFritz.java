package com.hbm.blocks.turret;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.turret.TileEntityTurretFritz;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TurretFritz extends TurretBaseNT {

	public TurretFritz(Material materialIn, String s){
		super(materialIn, s);
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		if(meta >= 12)
			return new TileEntityTurretFritz();
		return new TileEntityProxyCombo(true, true, false);
	}
}
