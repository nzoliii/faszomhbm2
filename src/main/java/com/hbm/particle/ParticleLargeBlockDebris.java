package com.hbm.particle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.world.World;

public class ParticleLargeBlockDebris extends ParticleBlockDust {
    public ParticleLargeBlockDebris(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, IBlockState state, int maxAge) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

        this.particleMaxAge = maxAge;

    }
}
