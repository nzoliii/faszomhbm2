package com.hbm.world.feature;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SchistStratum {

	NoiseGeneratorPerlin noise;

	IBlockState b;
	double scale;
	double threshold;
	double thickness;
	int heigth;

	public SchistStratum(IBlockState s, double scale, double threshold, double thickness, int heigth){
		this.b = s;
		this.scale = scale;
		this.threshold = threshold;
		this.thickness = thickness;
		this.heigth = heigth;
	}

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {

		if(this.noise == null) {
			this.noise = new NoiseGeneratorPerlin(event.getRand(), 4);
		}

		World world = event.getWorld();
		
		if(world.provider.getDimension() != 0)
			return;
		
		int cX = event.getChunkPos().x * 16;
		int cZ = event.getChunkPos().z * 16;

		for(int x = cX; x < cX + 16; x++) {
			for(int z = cZ; z < cZ + 16; z++) {
				
				double n = noise.getValue(x * scale, z * scale);
				if(n > threshold) {
					double range = (n - threshold) * (thickness * 0.5 -1);
					
					if(range > thickness * 0.5)
						range = thickness - range;
					
					if(range < 0)
						continue;
					
					int r = (int)range;
					
					for(int y = heigth - r; y <= heigth + r; y++) {
						
						IBlockState target = world.getBlockState(new BlockPos(x, y, z));
						
						if(target.isNormalCube() && target.getMaterial() == Material.ROCK) {
							world.setBlockState(new BlockPos(x, y, z), b);
						}
					}
				}
			}
		}
	}
}