package com.hbm.explosion.vanillant.interfaces;

import com.hbm.explosion.vanillant.ExplosionVNT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;

public interface IPlayerProcessor {

	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashMap<EntityPlayer, Vec3d> affectedPlayers);
}
