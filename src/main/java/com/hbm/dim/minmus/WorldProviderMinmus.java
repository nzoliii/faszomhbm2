package com.hbm.dim.minmus;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.minmus.GenLayerMinmus.GenLayerDiversifyMinmus;
import com.hbm.dim.minmus.GenLayerMinmus.GenLayerMinmusBasins;
import com.hbm.dim.minmus.GenLayerMinmus.GenLayerMinmusBiomes;
import com.hbm.dim.minmus.GenLayerMinmus.GenLayerMinmusPlains;
import net.minecraft.block.Block;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.layer.*;

public class WorldProviderMinmus extends WorldProviderCelestial {
	
	@Override
	public void init() {
		this.biomeProvider = new WorldChunkManagerCelestial(createBiomeGenerators(world.getSeed()));
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderMinmus(this.world, this.getSeed(), false);
	}

	@Override
	public Block getStone() {
		return ModBlocks.minmus_stone;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerMinmusBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerZoom(2001L, biomes);
		biomes = new GenLayerDiversifyMinmus(1000L, biomes);
		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerDiversifyMinmus(1001L, biomes);
		biomes = new GenLayerZoom(1001L, biomes);
		biomes = new GenLayerMinmusBasins(3000L, biomes);
		biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerMinmusPlains(200L, biomes);
		biomes = new GenLayerZoom(1006L, biomes);
		
		GenLayer genLayerVoronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVoronoiZoom, seed);
	}

	@Override
	public DimensionType getDimensionType(){return DimensionType.getById(SpaceConfig.minmusDimension);}

}