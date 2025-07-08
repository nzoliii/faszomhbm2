package com.hbm.explosion.vanillant.interfaces;

import com.hbm.explosion.vanillant.ExplosionVNT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;

public interface IBlockAllocator {

	public HashSet<BlockPos> allocate(ExplosionVNT explosion, World world, double x, double y, double z, float size);
}
