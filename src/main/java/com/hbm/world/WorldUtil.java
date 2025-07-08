package com.hbm.world;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;

import java.util.Map;

public class WorldUtil {
    public static Chunk provideChunk(WorldServer world, int chunkX, int chunkZ) {
        try {
            ChunkProviderServer provider = world.getChunkProvider();
            Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
            if(chunk != null) return chunk;
            return loadChunk(world, provider, chunkX, chunkZ);
        } catch(Throwable x) {
            return null;
        }
    }

    private static Chunk loadChunk(WorldServer world, ChunkProviderServer provider, int chunkX, int chunkZ) {
        long chunkCoord = ChunkPos.asLong(chunkX, chunkZ);
        provider.droppedChunks.remove(chunkCoord);
        Chunk chunk = provider.loadedChunks.get(chunkCoord);
        AnvilChunkLoader loader = null;

        if(provider.chunkLoader instanceof AnvilChunkLoader) {
            loader = (AnvilChunkLoader) provider.chunkLoader;
        }

        if(chunk == null && loader != null && loader.chunkExists(world, chunkX, chunkZ)) {
            chunk = ChunkIOExecutor.syncChunkLoad(world, loader, provider, chunkX, chunkZ);
        }

        return chunk;
    }
}
