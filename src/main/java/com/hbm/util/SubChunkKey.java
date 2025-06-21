package com.hbm.util;

import net.minecraft.util.math.ChunkPos;

/**
 * Unique identifier for sub-chunks.
 *
 * @author mlbv
 */
public class SubChunkKey {

    private int chunkXPos;
    private int chunkZPos;
    private int subY;
    private int hash;

    public SubChunkKey(int cx, int cz, int sy) {
        this.update(cx, cz, sy);
    }

    public SubChunkKey(ChunkPos pos, int sy) {
        this.update(pos.x, pos.z, sy);
    }

    public SubChunkKey update(int cx, int cz, int sy) {
        this.chunkXPos = cx;
        this.chunkZPos = cz;
        this.subY = sy;
        int result = subY;
        result = 31 * result + cx;
        result = 31 * result + cz;
        this.hash = result;
        return this;
    }

    @Override
    public final int hashCode() {
        return this.hash;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubChunkKey k)) return false;
        return this.subY == k.subY && this.chunkXPos == k.chunkXPos && this.chunkZPos == k.chunkZPos;
    }

    public int getSubY() {
        return subY;
    }

    public int getChunkXPos() {
        return chunkXPos;
    }

    public int getChunkZPos() {
        return chunkZPos;
    }

    public ChunkPos getPos() {
        return new ChunkPos(this.chunkXPos, this.chunkZPos);
    }
}