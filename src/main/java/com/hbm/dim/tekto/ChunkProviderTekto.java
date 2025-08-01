package com.hbm.dim.tekto;

import com.hbm.blocks.ModBlocks;
import com.hbm.dim.ChunkProviderCelestial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;

import javax.annotation.Nullable;


public class ChunkProviderTekto extends ChunkProviderCelestial {
	
	public ChunkProviderTekto(World world, long seed, boolean hasMapFeatures) {
		super(world, seed, hasMapFeatures);
		reclamp = false;
		stoneBlock = ModBlocks.basalt;
		seaBlock = ModBlocks.sulfuric_acid_block;
	}

	@Override
	public ChunkPrimer getChunkPrimer(int x, int z) {
		ChunkPrimer buffer = super.getChunkPrimer(x, z);

		// how many times do I gotta say BEEEEG
		return buffer;
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z){return false;}
	@Override
	@Nullable
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored){return null;}
	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z){};
	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos){return false;}
}