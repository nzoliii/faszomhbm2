package com.hbm.explosion.vanillant.interfaces;

import com.hbm.explosion.vanillant.ExplosionVNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;

public interface IEntityProcessor {

	public HashMap<EntityPlayer, Vec3d> process(ExplosionVNT explosion, World world, double x, double y, double z, float size);
}
