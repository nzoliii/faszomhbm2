package com.hbm.handler.guncfg;

import com.hbm.entity.projectile.EntityBulletBase;
import com.hbm.explosion.ExplosionChaos;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.handler.BulletConfiguration;
import com.hbm.handler.GunConfiguration;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.IBulletImpactBehavior;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.potion.HbmPotion;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations.AnimType;
import com.hbm.render.misc.RenderScreenOverlay.Crosshair;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.ArrayList;

public class GunEnergyFactory {

	public static GunConfiguration getZOMGConfig() {

		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 1;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.reloadDuration = 10;
		config.reloadSoundEnd = false;
		config.firingDuration = 0;
		config.durability = 100000;
		config.reloadType = GunConfiguration.RELOAD_FULL;
		config.ammoCap = 1000;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.L_ARROWS;
		config.firingSound = HBMSoundHandler.zomgShoot;
		config.reloadSound = HBMSoundHandler.b92Reload;

		config.name = "EMC101 Prismatic Negative Energy Cannon";
		config.manufacturer = "MWT Prototype Labs";

		config.comment.add("Taste the rainbow!");

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.ZOMG_BOLT);

		return config;
	}

	public static GunConfiguration getEMPConfig() {

		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 30;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_MANUAL;
		config.reloadDuration = 10;
		config.firingDuration = 0;
		config.ammoCap = 0;
		config.durability = 1500;
		config.reloadType = GunConfiguration.RELOAD_NONE;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.L_SPLIT;
		config.firingSound = HBMSoundHandler.teslaShoot;

		config.name = "EMP Orb Projector";
		config.manufacturer = "MWT Prototype Labs";

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.SPECIAL_EMP);

		return config;
	}

	public static BulletConfiguration getOrbusConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = ModItems.gun_emp_ammo;

		bullet.velocity = 1F;
		bullet.spread = 0.0F;
		bullet.wear = 10;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.dmgMin = 10;
		bullet.dmgMax = 12;
		bullet.gravity = 0D;
		bullet.maxAge = 100;
		bullet.doesRicochet = false;
		bullet.doesPenetrate = true;
		bullet.doesBreakGlass = false;
		bullet.style = BulletConfiguration.STYLE_ORB;
		bullet.plink = BulletConfiguration.PLINK_NONE;
		bullet.emp = 10;

		bullet.effects = new ArrayList<PotionEffect>();
		bullet.effects.add(new PotionEffect(MobEffects.SLOWNESS, 10 * 20, 1));
		bullet.effects.add(new PotionEffect(MobEffects.WEAKNESS, 10 * 20, 4));

		return bullet;
	}

	public static GunConfiguration getFlamerConfig() {

		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 1;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.reloadDuration = 20;
		config.reloadSoundEnd = false;
		config.firingDuration = 0;
		config.ammoCap = 100;
		config.durability = 1000;
		config.reloadType = GunConfiguration.RELOAD_FULL;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.L_CIRCLE;
		config.firingSound = HBMSoundHandler.flamethrowerShoot;
		config.reloadSound = HBMSoundHandler.flamerReload;

		config.name = "Heavy Duty Flamer";
		config.manufacturer = "MWT Prototype Labs";

		config.comment.add("Dragon-slaying: Advanced techniques, part 1:");
		config.comment.add("Try not to get eaten by the dragon.");
		config.comment.add("");
		config.comment.add("Hope that helps.");

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.FLAMER_NORMAL);
		config.config.add(BulletConfigSyncingUtil.FLAMER_NAPALM);
		config.config.add(BulletConfigSyncingUtil.FLAMER_WP);
		config.config.add(BulletConfigSyncingUtil.FLAMER_VAPORIZER);
		config.config.add(BulletConfigSyncingUtil.FLAMER_GAS);

		return config;
	}

	public static GunConfiguration getVortexConfig() {

		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 30;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.hasSights = false;
		config.reloadDuration = 20;
		config.firingDuration = 0;
		config.ammoCap = 10;
		config.reloadType = GunConfiguration.RELOAD_FULL;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.NONE;
		config.durability = 10000;
		config.reloadSound = GunConfiguration.RSOUND_MAG;
		config.firingSound = HBMSoundHandler.hksShoot;
		config.reloadSoundEnd = false;

		config.name = "Visual Operation Ranged Tactical Electromagnetic Xenoblaster";
		config.manufacturer = "Xon Corporation";

		config.comment.add("OBEY XON");
		
		config.animations.put(AnimType.CYCLE, new BusAnimation()
				.addBus("VORTEX_RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 1, -5, 25))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 400))
						));

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.R556_STAR);

		return config;

	}
	
	public static GunConfiguration getCCPlasmaGunConfig() {
		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 2;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.hasSights = false;
		config.reloadDuration = 20;
		config.firingDuration = 0;
		config.ammoCap = 40;
		config.reloadType = GunConfiguration.RELOAD_NONE;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.NONE;
		config.durability = 10000;
		config.reloadSound = GunConfiguration.RSOUND_MAG;
		config.firingSound = HBMSoundHandler.osiprShoot;
		config.reloadSoundEnd = false;

		config.name = "ChickenCom Light Duty Plasma Gun";
		config.manufacturer = "ChickenCom";

		config.comment.add("A gun originally manufactured for a lesser species.");
		
		config.animations.put(AnimType.CYCLE, new BusAnimation()
				.addBus("RECOIL", new BusAnimationSequence()
						.addKeyframe(new BusAnimationKeyframe(0, 1, -5, 25))
						.addKeyframe(new BusAnimationKeyframe(0, 0, 0, 200))
						));

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.R556_NORMAL);
		config.config.add(BulletConfigSyncingUtil.R556_GOLD);
		config.config.add(BulletConfigSyncingUtil.R556_TRACER);
		config.config.add(BulletConfigSyncingUtil.R556_PHOSPHORUS);
		config.config.add(BulletConfigSyncingUtil.R556_AP);
		config.config.add(BulletConfigSyncingUtil.R556_DU);
		config.config.add(BulletConfigSyncingUtil.R556_STAR);
		config.config.add(BulletConfigSyncingUtil.CHL_R556);
		config.config.add(BulletConfigSyncingUtil.R556_SLEEK);
		config.config.add(BulletConfigSyncingUtil.R556_K);

		return config;
	}
	
	public static GunConfiguration getEgonConfig() {
		GunConfiguration config = new GunConfiguration();

		config.rateOfFire = 2;
		config.roundsPerCycle = 1;
		config.gunMode = GunConfiguration.MODE_NORMAL;
		config.firingMode = GunConfiguration.FIRE_AUTO;
		config.hasSights = false;
		config.reloadDuration = 20;
		config.firingDuration = 0;
		config.ammoCap = 40;
		config.reloadType = GunConfiguration.RELOAD_NONE;
		config.allowsInfinity = true;
		config.crosshair = Crosshair.NONE;
		config.durability = 10000;
		config.reloadSound = GunConfiguration.RSOUND_MAG;
		config.firingSound = HBMSoundHandler.osiprShoot;
		config.reloadSoundEnd = false;

		config.name = "Gluon Gun";
		config.manufacturer = "Black Mesa Research Facility";

		config.comment.add("Damage starts at 5/s and gets doubled every 2s while on target");
		config.comment.add("Working to make a better tomorrow for all mankind.");

		config.config = new ArrayList<Integer>();
		config.config.add(BulletConfigSyncingUtil.SPECIAL_GAUSS);

		return config;
	}

	public static BulletConfiguration getFlameConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = ModItems.ammo_fuel;
		bullet.ammoCount = 100;

		bullet.velocity = 0.75F;
		bullet.spread = 0.025F;
		bullet.wear = 1;
		bullet.bulletsMin = 3;
		bullet.bulletsMax = 5;
		bullet.dmgMin = 2;
		bullet.dmgMax = 4;
		bullet.gravity = 0.01D;
		bullet.maxAge = 60;
		bullet.doesRicochet = false;
		bullet.doesPenetrate = true;
		bullet.doesBreakGlass = false;
		bullet.style = BulletConfiguration.STYLE_NONE;
		bullet.plink = BulletConfiguration.PLINK_NONE;
		bullet.vPFX = "flame";
		bullet.incendiary = 10;

		bullet.bImpact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {

				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaburst");
				data.setString("mode", "flame");
				data.setInteger("count", 15);
				data.setDouble("motion", 0.1D);

				/*
				 * java.lang.NullPointerException
				 *	at cpw.mods.fml.common.network.FMLOutboundHandler$OutboundTarget$7.selectNetworks(FMLOutboundHandler.java:193)
				 *	at cpw.mods.fml.common.network.FMLOutboundHandler.write(FMLOutboundHandler.java:273)
				 *	at io.netty.channel.DefaultChannelHandlerContext.invokeWrite(DefaultChannelHandlerContext.java:644)
				 *	at io.netty.channel.DefaultChannelHandlerContext.write(DefaultChannelHandlerContext.java:698)
				 *	at io.netty.channel.DefaultChannelHandlerContext.write(DefaultChannelHandlerContext.java:637)
				 *	at io.netty.handler.codec.MessageToMessageEncoder.write(MessageToMessageEncoder.java:115)
				 *	at io.netty.handler.codec.MessageToMessageCodec.write(MessageToMessageCodec.java:116)
				 *	at io.netty.channel.DefaultChannelHandlerContext.invokeWrite(DefaultChannelHandlerContext.java:644)
				 *	at io.netty.channel.DefaultChannelHandlerContext.write(DefaultChannelHandlerContext.java:698)
				 *	at io.netty.channel.DefaultChannelHandlerContext.writeAndFlush(DefaultChannelHandlerContext.java:688)
				 *	at io.netty.channel.DefaultChannelHandlerContext.writeAndFlush(DefaultChannelHandlerContext.java:717)
				 *	at io.netty.channel.DefaultChannelPipeline.writeAndFlush(DefaultChannelPipeline.java:893)
				 *	at io.netty.channel.AbstractChannel.writeAndFlush(AbstractChannel.java:239)
				 *	at cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper.sendToAllAround(SimpleNetworkWrapper.java:210)
				 *	at com.hbm.handler.guncfg.GunEnergyFactory$1.behaveBlockHit(GunEnergyFactory.java:150)
				 */
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, bullet.posX, bullet.posY, bullet.posZ), new TargetPoint(bullet.dimension, bullet.posX, bullet.posY, bullet.posZ, 50));
			}
		};

		return bullet;
	}

	public static BulletConfiguration getNapalmConfig() {

		BulletConfiguration bullet = getFlameConfig();

		bullet.ammo = ModItems.ammo_fuel_napalm;
		bullet.wear = 2;
		bullet.dmgMin = 4;
		bullet.dmgMax = 6;
		bullet.maxAge = 200;

		return bullet;
	}

	public static BulletConfiguration getPhosphorusConfig() {

		BulletConfiguration bullet = getFlameConfig();

		bullet.ammo = ModItems.ammo_fuel_phosphorus;
		bullet.wear = 2;
		bullet.spread = 0.0F;
		bullet.bulletsMin = 1;
		bullet.bulletsMax = 1;
		bullet.dmgMin = 4;
		bullet.dmgMax = 6;
		bullet.maxAge = 200;
		bullet.vPFX = "smoke";

		bullet.bImpact = BulletConfigFactory.getPhosphorousEffect(5, 60 * 20, 25, 0.25, 0.1F);

		return bullet;
	}

	public static BulletConfiguration getVaporizerConfig() {

		BulletConfiguration bullet = getFlameConfig();

		bullet.ammo = ModItems.ammo_fuel_vaporizer;
		bullet.wear = 4;
		bullet.spread = 0.25F;
		bullet.bulletsMin = 8;
		bullet.bulletsMax = 10;
		bullet.dmgMin = 6;
		bullet.dmgMax = 10;
		bullet.maxAge = 15;
		bullet.vPFX = "flame";
		bullet.incendiary = 0;

		PotionEffect eff = new PotionEffect(HbmPotion.phosphorus, 20 * 20, 0, true, false);
		eff.getCurativeItems().clear();
		bullet.effects = new ArrayList<>();
		bullet.effects.add(new PotionEffect(eff));

		return bullet;
	}

	public static BulletConfiguration getGasConfig() {

		BulletConfiguration bullet = getFlameConfig();

		bullet.ammo = ModItems.ammo_fuel_gas;
		bullet.wear = 1;
		bullet.spread = 0.05F;
		bullet.gravity = 0D;
		bullet.bulletsMin = 5;
		bullet.bulletsMax = 7;
		bullet.dmgMin = 0;
		bullet.dmgMax = 0;
		bullet.vPFX = "cloud";
		bullet.incendiary = 0;

		bullet.bImpact = BulletConfigFactory.getGasEffect(5, 60 * 20);

		return bullet;
	}

	public static BulletConfiguration getZOMGBoltConfig() {

		BulletConfiguration bullet = new BulletConfiguration();

		bullet.ammo = ModItems.nugget_euphemium;
		bullet.ammoCount = 1000;
		bullet.wear = 1;
		bullet.velocity = 1F;
		bullet.spread = 0.125F;
		bullet.maxAge = 100;
		bullet.gravity = 0D;
		bullet.bulletsMin = 5;
		bullet.bulletsMax = 5;
		bullet.dmgMin = 10000;
		bullet.dmgMax = 25000;

		bullet.style = BulletConfiguration.STYLE_BOLT;
		bullet.trail = bullet.BOLT_ZOMG;

		bullet.effects = new ArrayList<>();
		bullet.effects.add(new PotionEffect(HbmPotion.bang, 10 * 20, 0));

		bullet.bImpact = new IBulletImpactBehavior() {

			@Override
			public void behaveBlockHit(EntityBulletBase bullet, int x, int y, int z) {

				if(!bullet.world.isRemote) {
					ExplosionChaos.explodeZOMG(bullet.world, (int) bullet.posX, (int) bullet.posY, (int) bullet.posZ, 5);
					bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, HBMSoundHandler.bombDet, SoundCategory.HOSTILE, 5.0F, 1.0F);
					ExplosionLarge.spawnParticles(bullet.world, bullet.posX, bullet.posY, bullet.posZ, 5);
				}
			}
		};

		return bullet;
	}
	
	public static BulletConfiguration getTurretConfig() {
		BulletConfiguration bullet = getFlameConfig();
		bullet.spread *= 2F;
		bullet.gravity = 0.0025D;
		return bullet;
	}

}