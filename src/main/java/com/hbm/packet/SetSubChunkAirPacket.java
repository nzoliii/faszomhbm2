package com.hbm.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SetSubChunkAirPacket implements IMessage {

    private int chunkX;
    private int chunkZ;
    private int subY;

    public SetSubChunkAirPacket() {
    }

    public SetSubChunkAirPacket(int chunkX, int chunkZ, int subY) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.subY = subY;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeByte(subY);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        subY = buf.readUnsignedByte();
    }

    @SideOnly(Side.CLIENT)
    public static class Handler implements IMessageHandler<SetSubChunkAirPacket, IMessage> {

        @Override
        public IMessage onMessage(SetSubChunkAirPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message));
            return null;
        }

        private void handle(SetSubChunkAirPacket message) {
            WorldClient world = Minecraft.getMinecraft().world;
            if (world == null) return;
            Chunk chunk = world.getChunkProvider().provideChunk(message.chunkX, message.chunkZ);
            if (chunk.isEmpty()) return;
            ExtendedBlockStorage[] storages = chunk.getBlockStorageArray();
            if (message.subY < 0 || message.subY >= storages.length) return;
            ExtendedBlockStorage storage = storages[message.subY];
            if (storage == null || storage.isEmpty()) return;
            IBlockState airState = Blocks.AIR.getDefaultState();
            for (int ly = 0; ly < 16; ly++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        storage.set(lx, ly, lz, airState);
                    }
                }
            }
            storage.recalculateRefCounts();
            chunk.setModified(true);
            int minY = message.subY << 4;
            world.markBlockRangeForRenderUpdate(message.chunkX << 4, minY, message.chunkZ << 4, (message.chunkX << 4) + 15, minY + 15,
                    (message.chunkZ << 4) + 15);
            chunk.generateSkylightMap();
        }
    }
}