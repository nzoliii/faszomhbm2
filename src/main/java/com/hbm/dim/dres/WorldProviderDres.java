package com.hbm.dim.dres;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.WorldChunkManagerCelestial;
import com.hbm.dim.WorldChunkManagerCelestial.BiomeGenLayers;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.dim.dres.GenLayerDres.GenLayerDiversifyDres;
import com.hbm.dim.dres.GenLayerDres.GenLayerDresBasins;
import com.hbm.dim.dres.GenLayerDres.GenLayerDresBiomes;
import com.hbm.dim.dres.GenLayerDres.GenLayerDresPlains;
import net.minecraft.block.Block;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.layer.*;

public class WorldProviderDres extends WorldProviderCelestial {
	
	@Override
	public void init() {
		this.biomeProvider = new WorldChunkManagerCelestial(createBiomeGenerators(world.getSeed()));
	}
	
	@Override
	public IChunkGenerator createChunkGenerator() {
		return new ChunkProviderDres(this.world, this.getSeed(), false);
	}
	
	// sorry mellow...
	// OOH I AM FOR REAL
	// NEVER MEANT TO MAKE YOUR DAUGHTER CRY
	@Override
	public Block getStone() {
		return ModBlocks.dres_rock;
	}

	private static BiomeGenLayers createBiomeGenerators(long seed) {
		GenLayer biomes = new GenLayerDresBiomes(seed);

		biomes = new GenLayerFuzzyZoom(2000L, biomes);
		biomes = new GenLayerZoom(2001L, biomes);
		biomes = new GenLayerDiversifyDres(1000L, biomes);
		biomes = new GenLayerZoom(1000L, biomes);
		biomes = new GenLayerDiversifyDres(1001L, biomes);
		biomes = new GenLayerZoom(1001L, biomes);
		biomes = new GenLayerDresBasins(3000L, biomes);
		biomes = new GenLayerZoom(1003L, biomes);
		biomes = new GenLayerSmooth(700L, biomes);
		biomes = new GenLayerDresPlains(200L, biomes);

		biomes = new GenLayerZoom(1006L, biomes);
		 
		GenLayer genLayerVeronoiZoom = new GenLayerVoronoiZoom(10L, biomes);

		return new BiomeGenLayers(biomes, genLayerVeronoiZoom, seed);
	}

	@Override
	public DimensionType getDimensionType(){return DimensionType.getById(SpaceConfig.dresDimension);}

}