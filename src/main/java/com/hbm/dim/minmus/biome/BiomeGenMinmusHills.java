package com.hbm.dim.minmus.biome;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

import java.util.Random;

public class BiomeGenMinmusHills extends BiomeGenBaseMinmus {

	public BiomeGenMinmusHills(BiomeProperties properties) {
		super(properties);
        this.topBlock = ModBlocks.minmus_regolith.getDefaultState();
        this.fillerBlock = ModBlocks.minmus_regolith.getDefaultState();
	}

	@Override
	public void genTerrainBlocks(World world, Random rand, ChunkPrimer chunkPrimer, int x, int z, double noise) {
		IBlockState topBlockState = this.topBlock;
		IBlockState fillerBlockState = this.fillerBlock;
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + rand.nextDouble() * 0.25D);
		int i1 = x & 15;
		int j1 = z & 15;
		int maxHeight = 256;

		for (int l1 = maxHeight - 1; l1 >= 0; --l1) {
			BlockPos pos = new BlockPos(x, l1, z);
			IBlockState currentState = chunkPrimer.getBlockState(i1, l1, j1);

			if (l1 <= 0 + rand.nextInt(5)) {
				chunkPrimer.setBlockState(i1, l1, j1, Blocks.BEDROCK.getDefaultState());
			} else {
				if (currentState.getBlock() != Blocks.AIR) {
					if (currentState.getBlock() == ModBlocks.minmus_stone) {
						if (k == -1) {
							if (l <= 0) {
								topBlockState = Blocks.AIR.getDefaultState();
								fillerBlockState = ModBlocks.minmus_stone.getDefaultState();
							} else if (l1 >= 59 && l1 <= 64) {
								topBlockState = this.topBlock;
								fillerBlockState = this.fillerBlock;
							}

							if (l1 < 63 && (topBlockState.getBlock() == Blocks.AIR)) {
								if (this.getTemperature(pos) < 0.15F) {
									topBlockState = this.topBlock;
								} else {
									topBlockState = this.topBlock;
								}
							}

							k = l;

							if (l1 >= 62) {
								chunkPrimer.setBlockState(i1, l1, j1, topBlockState);
							} else if (l1 < 62) {
								topBlockState = Blocks.AIR.getDefaultState();
								fillerBlockState = ModBlocks.minmus_stone.getDefaultState();
								chunkPrimer.setBlockState(i1, l1, j1, Blocks.GRAVEL.getDefaultState());
							} else {
								chunkPrimer.setBlockState(i1, l1, j1, fillerBlockState);
							}
						} else if (k > 0) {
							--k;
							chunkPrimer.setBlockState(i1, l1, j1, fillerBlockState);

							if (k == 0 && fillerBlockState.getBlock() == Blocks.SAND) {
								k = rand.nextInt(4) + Math.max(0, l1 - 63);
								fillerBlockState = Blocks.SANDSTONE.getDefaultState();
							}
						}
					}
				} else {
					k = -1;
				}
			}
		}
	}

}