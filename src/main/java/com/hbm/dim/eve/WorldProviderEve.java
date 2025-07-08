package com.hbm.dim.eve;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveBiomes;
import com.hbm.dim.eve.GenLayerEve.GenLayerEveRiverMix;
import com.hbm.lib.HBMSoundHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldProviderEve extends WorldProviderCelestial {

	@Override
	public void init() {
		this.biomeProvider = new WorldChunkManagerCelestial(createBiomeGenerators(world.getSeed()));
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderEve(this.world, this.getSeed(), false);
	}


	private int chargetime;
	private float flashd;

	@Override
	public void updateWeather() {
		super.updateWeather();

		if(!world.isRemote) {
			if (chargetime <= 0 || chargetime <= 800) {
				chargetime += 1;
			} else if (chargetime >= 800) {
				chargetime = 0;
			}
		} else {
			if (chargetime >= 800) {
				flashd = 0;
			} else if (chargetime >= 100) {
				if (flashd <= 1) {
					Minecraft.getMinecraft().player.playSound(HBMSoundHandler.rumble, 10F, 1F);
				}
				flashd += 0.1f;
				flashd = Math.min(100.0f, flashd + 0.1f * (100.0f - flashd) * 0.15f);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("chargetime", chargetime);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		chargetime = nbt.getInteger("chargetime");
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(chargetime);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		chargetime = buf.readInt();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3d getSkyColor(Entity camera, float partialTicks) {
		Vec3d ohshit = super.getSkyColor(camera, partialTicks);
		float alpha = (flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, flashd / 100);

		return new Vec3d(ohshit.x + alpha , ohshit.y + alpha, ohshit.z + alpha);
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1) {
		float imsuper = super.getSunBrightness(par1);
		float alpha = (flashd <= 0) ? 0.0F : 1.0F - Math.min(1.0F, flashd / 100);

		return imsuper + alpha * 0.7F;
	}

	@Override
	public Block getStone() {
		return ModBlocks.eve_rock;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer genlayerBiomes = new GenLayerEveBiomes(seed); // Your custom biome layer

		genlayerBiomes = new GenLayerZoom(1000L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1001L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1002L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1003L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1004L, genlayerBiomes);
		genlayerBiomes = new GenLayerZoom(1005L, genlayerBiomes);

		GenLayer genlayerRiverZoom = new GenLayerZoom(1000L, genlayerBiomes);
		GenLayer genlayerRiver = new GenLayerRiver(1001L, genlayerRiverZoom); // Your custom river layer
		GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerRiver);

		GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerBiomes);
		GenLayerEveRiverMix genlayerrivermix = new GenLayerEveRiverMix(100L, genlayersmooth1, genlayersmooth);
		GenLayerVoronoiZoom genlayervoronoizoom = new GenLayerVoronoiZoom(10L, genlayerrivermix);
		
		return new BiomeGenLayers(genlayerrivermix, genlayervoronoizoom, seed);
	}

	@Override
	public DimensionType getDimensionType(){return DimensionType.getById(SpaceConfig.eveDimension);}

}