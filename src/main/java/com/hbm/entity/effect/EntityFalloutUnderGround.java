package com.hbm.entity.effect;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityFalloutUnderGround extends EntityFallout {

    private final double phi;
    public int falloutRainRadius1 = 0;
    public int falloutRainRadius2 = 0;
    public boolean falloutRainDoFallout = false;
    public boolean falloutRainDoFlood = false;
    public boolean falloutRainFire = false;
    int age = 0;
    private int maxSamples;
    private int currentSample;
    private int radius;

    public EntityFalloutUnderGround(World world) {
        super(world);
        this.setSize(4, 20);
        this.phi = Math.PI * (3 - Math.sqrt(5));
        this.done = false;
        this.currentSample = 0;
    }

    @Override
    protected void calculateS(int i) {
        s0 = 0.84 * i;
        s1 = 0.74 * i;
        s2 = 0.64 * i;
        s3 = 0.54 * i;
        s4 = 0.44 * i;
        s5 = 0.34 * i;
        s6 = 0.24 * i;
    }

    public void setScale(int i) {
        dataManager.set(SCALE, i);
        calculateS(i);
        radius = i;
        maxSamples = (int) (Math.PI * (long) i * i);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (!CompatibilityConfig.isWarDim(world)) {
                done = true;
                unloadAllChunks();
                setDead();
                return;
            }
            age++;
            if (GeneralConfig.enableExtendedLogging && age == 120) {
                MainRegistry.logger.info("NTM F {} {}% {}/{}", currentSample, String.format("%.4f", 100.0 * currentSample / (double) this.maxSamples), currentSample, this.maxSamples);
                age = 0;
            }
            int rayCounter = 0;
            long start = System.currentTimeMillis();
            final long timeBudget = BombConfig.mk5;
            double fy, fr, theta;
            for (int sample = currentSample; sample < this.maxSamples; sample++) {
                this.currentSample = sample;
                if (rayCounter > 0 && rayCounter % 50 == 0 && System.currentTimeMillis() > start + timeBudget) {
                    break;
                }
                fy = (2D * sample / (maxSamples - 1D)) - 1D;
                fr = Math.sqrt(1D - fy * fy);
                theta = phi * sample;
                double dx = Math.cos(theta) * fr;
                double dy = fy;
                double dz = Math.sin(theta) * fr;
                Vec3d vecStart = new Vec3d(posX, posY, posZ);
                Vec3d vecEnd = vecStart.add(dx * radius, dy * radius, dz * radius);
                traverseRay(vecStart, vecEnd, p -> {
                    MutableBlockPos mp = new MutableBlockPos(p);
                    double dist = vecStart.distanceTo(new Vec3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5));
                    if (p.getY() < 0 || p.getY() > 255) return false;
                    return processBlock(world, mp, dist, false, 0, 0, false, false, 0);
                });
                rayCounter++;
            }
            if (this.currentSample >= this.maxSamples - 1) {
                if (falloutRainRadius1 > 0) {
                    EntityFalloutRain falloutRain = new EntityFalloutRain(this.world);
                    falloutRain.doFallout = falloutRainDoFallout;
                    falloutRain.doFlood = falloutRainDoFlood;
                    falloutRain.posX = this.posX;
                    falloutRain.posY = this.posY;
                    falloutRain.posZ = this.posZ;
                    falloutRain.spawnFire = falloutRainFire;
                    falloutRain.setScale(falloutRainRadius1, falloutRainRadius2);
                    this.world.spawnEntity(falloutRain);
                }
                unloadAllChunks();
                this.setDead();
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        setScale(nbt.getInteger("scale"));
        currentSample = nbt.getInteger("currentSample");
        falloutRainRadius1 = nbt.getInteger("fR1");
        falloutRainRadius2 = nbt.getInteger("fR2");
        falloutRainDoFallout = nbt.getBoolean("fRfallout");
        falloutRainDoFlood = nbt.getBoolean("fRflood");
        falloutRainFire = nbt.getBoolean("fRfire");
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("scale", getScale());
        nbt.setInteger("currentSample", currentSample);
        nbt.setInteger("fR1", falloutRainRadius1);
        nbt.setInteger("fR2", falloutRainRadius2);
        nbt.setBoolean("fRfallout", falloutRainDoFallout);
        nbt.setBoolean("fRflood", falloutRainDoFlood);
        nbt.setBoolean("fRfire", falloutRainFire);
    }
}