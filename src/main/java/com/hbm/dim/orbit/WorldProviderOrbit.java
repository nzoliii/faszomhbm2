package com.hbm.dim.orbit;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_Destroyed;
import com.hbm.handler.atmosphere.ChunkAtmosphereManager;
import com.hbm.lib.Library;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BobMathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderOrbit extends WorldProvider {

	// Orbit at an altitude that provides an hour-long realtime orbit (game time is fast so we go slow)
	// We want a consistent orbital period to prevent orbiting too slow or fast (both for player comfort and feel)
	private static final float ORBITAL_PERIOD = 7200;

	protected float getOrbitalAltitude(CelestialBody body) {
		return getAltitudeForPeriod(body.massKg, ORBITAL_PERIOD);
	}
	
	// r = ∛[(G x Me x T2) / (4π2)]
	private float getAltitudeForPeriod(float massKg, float period) {
		return (float)Math.cbrt((AstronomyUtil.GRAVITATIONAL_CONSTANT * massKg * (period * period)) / (4 * Math.PI * Math.PI));
	}

	public float getSunPower() {
		double progress = OrbitalStation.clientStation.getTransferProgress(0);
		float sunPower = OrbitalStation.clientStation.orbiting.getSunPower();
		if(progress > 0) {
			return (float)BobMathUtil.lerp(progress, sunPower, OrbitalStation.clientStation.target.getSunPower());
		}
		return sunPower;
	}

	@Override
	public void init() {
		this.biomeProvider = new BiomeProviderSingle(new BiomeGenOrbit(new Biome.BiomeProperties("Space").setRainDisabled()));
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderOrbit(this.world);
	}

	@Override
	public void updateWeather() {
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getFogColor(float x, float y) {
		return new Vec3d(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getSkyColor(Entity camera, float partialTicks) {
		return new Vec3d(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1) {
		// Stars look cool in orbit, but obvs at Moho we don't want the big fuckoff sun to not extinguish
		// Stars become visible during the day part of orbit just before Earth
		// And are fully visible during the day beyond the orbit of Duna
		float distanceStart = 9_000_000;
		float distanceEnd = 30_000_000;

		double progress = OrbitalStation.clientStation.getTransferProgress(par1);
		float semiMajorAxisKm = OrbitalStation.clientStation.orbiting.getPlanet().semiMajorAxisKm;
		if(progress > 0) {
			semiMajorAxisKm = (float)BobMathUtil.lerp(progress, semiMajorAxisKm, OrbitalStation.clientStation.target.getPlanet().semiMajorAxisKm);
		}

		float distanceFactor = MathHelper.clamp((semiMajorAxisKm - distanceStart) / (distanceEnd - distanceStart), 0F, 1F);

		float celestialAngle = world.getCelestialAngle(par1);
		float celestialPhase = (1 - (celestialAngle + 0.5F) % 1) * 2 - 1;
		float starBrightness = (float)Library.smoothstep(Math.abs(celestialPhase), 0.6, 0.75);

		return MathHelper.clamp(starBrightness, distanceFactor, 1F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		if(SolarSystem.kerbol.hasTrait(CBT_Destroyed.class))
			return 0;

		float celestialAngle = world.getCelestialAngle(par1);
		float celestialPhase = (1 - (celestialAngle + 0.5F) % 1) * 2 - 1;

		return 1 - (float)Library.smoothstep(Math.abs(celestialPhase), 0.6, 0.8);
	}

	@Override
	public boolean canDoLightning(Chunk chunk) {
		return false;
	}

	@Override
	public boolean canDoRainSnowIce(Chunk chunk) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight() {
		return -99999;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer() {
		return new SkyProviderOrbit();
	}

	@Override
	public float calculateCelestialAngle(long worldTime, float partialTicks) {
		CelestialBody orbiting = OrbitalStation.clientStation.orbiting;
		CelestialBody target = OrbitalStation.clientStation.target;
		double progress = OrbitalStation.clientStation.getTransferProgress(partialTicks);
		float angle = (float)SolarSystem.calculateSingleAngle(world, partialTicks, orbiting, getOrbitalAltitude(orbiting));
		if(progress > 0) {
			angle = (float)BobMathUtil.lerp(progress, angle, (float)SolarSystem.calculateSingleAngle(world, partialTicks, target, getOrbitalAltitude(target)));
		}
		return 0.5F - (angle / 360.0F);
	}

	// Same shit as in Celestial
	@Override
	public int getRespawnDimension(EntityPlayerMP player) {
		BlockPos coords = player.getBedLocation(getDimension());

		// If no bed, respawn in overworld
		if(coords == null)
			return 0;

		// If the bed location has no breathable atmosphere, respawn in overworld
		CBT_Atmosphere atmosphere = ChunkAtmosphereManager.proxy.getAtmosphere(world, coords.getX(), coords.getY(), coords.getZ());
		if(!ChunkAtmosphereManager.proxy.canBreathe(atmosphere))
			return 0;

		return getDimension();
	}

	@Override
	public boolean canRespawnHere() {
		if(WorldProviderCelestial.attemptingSleep) {
			WorldProviderCelestial.attemptingSleep = false;
			return true;
		}

		return false;
	}

	@Override
	public DimensionType getDimensionType(){return DimensionType.getById(SpaceConfig.orbitDimension);}
	
}
