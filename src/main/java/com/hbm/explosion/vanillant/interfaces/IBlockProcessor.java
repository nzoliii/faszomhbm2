package com.hbm.explosion.vanillant.interfaces;

import com.hbm.explosion.vanillant.ExplosionVNT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

public interface IBlockProcessor {

	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashSet<BlockPos> affectedBlocks);
}
