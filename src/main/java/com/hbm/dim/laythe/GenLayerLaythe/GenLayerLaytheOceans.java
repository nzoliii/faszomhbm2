package com.hbm.dim.laythe.GenLayerLaythe;

import com.hbm.dim.laythe.biome.BiomeGenBaseLaythe;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerLaytheOceans extends GenLayer {

    public GenLayerLaytheOceans(long seed, GenLayer genLayer) {
        super(seed);
        this.parent = genLayer;
    }

    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        return this.getIntsOcean(areaX, areaY, areaWidth, areaHeight);
    }

    private int[] getIntsOcean(int p_151626_1_, int p_151626_2_, int p_151626_3_, int p_151626_4_) {
        int i = p_151626_1_ - 1;
        int j = p_151626_2_ - 1;
        int k = 1 + p_151626_3_ + 1;
        int l = 1 + p_151626_4_ + 1;
        int[] aint = this.parent.getInts(i, j, k, l);
        int[] aint1 = IntCache.getIntCache(p_151626_3_ * p_151626_4_);

        for(int i1 = 0; i1 < p_151626_4_; ++i1) {
            for(int j1 = 0; j1 < p_151626_3_; ++j1) {
                this.initChunkSeed((long) (j1 + p_151626_1_), (long) (i1 + p_151626_2_));
                int k1 = aint[j1 + 1 + (i1 + 1) * k];

                if(k1 == Biome.getIdForBiome(BiomeGenBaseLaythe.laytheOcean)) {
                    int l1 = aint[j1 + 1 + (i1 + 1 - 1) * k];
                    int i2 = aint[j1 + 1 + 1 + (i1 + 1) * k];
                    int j2 = aint[j1 + 1 - 1 + (i1 + 1) * k];
                    int k2 = aint[j1 + 1 + (i1 + 1 + 1) * k];
                    boolean flag = ((l1 == Biome.getIdForBiome(BiomeGenBaseLaythe.laytheIsland))
                            || (i2 == Biome.getIdForBiome(BiomeGenBaseLaythe.laytheIsland))
                            || (j2 == Biome.getIdForBiome(BiomeGenBaseLaythe.laytheIsland))
                            || (k2 == Biome.getIdForBiome(BiomeGenBaseLaythe.laytheIsland)));
                    if(flag) {
                        k1 = Biome.getIdForBiome(BiomeGenBaseLaythe.laytheOcean);
                    }
                }

                aint1[j1 + i1 * p_151626_3_] = k1;
            }
        }

        return aint1;
    }

}
