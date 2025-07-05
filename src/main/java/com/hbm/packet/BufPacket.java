package com.hbm.packet;

import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.PrecompiledPacket;
import com.hbm.tileentity.IBufPacketReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BufPacket extends PrecompiledPacket {

    int x;
    int y;
    int z;
    IBufPacketReceiver rec;
    ByteBuf buf;

    public BufPacket() { }

    public BufPacket(int x, int y, int z, IBufPacketReceiver rec) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rec = rec;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.buf = buf;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        this.rec.serialize(buf);
    }

    public static class Handler implements IMessageHandler<BufPacket, IMessage> {

        @Override
        public IMessage onMessage(BufPacket m, MessageContext ctx) {

            if(Minecraft.getMinecraft().world == null)
                return null;

            TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(m.x, m.y, m.z));

            if(te instanceof IBufPacketReceiver) {
                try {
                    ((IBufPacketReceiver) te).deserialize(m.buf);
                }  catch(Exception e) { // just in case I fucked up
                    MainRegistry.logger.warn("A ByteBuf packet failed to be read and has thrown an error. This normally means that there was a buffer underflow and more data was read than was actually in the packet.");
                    MainRegistry.logger.warn("Tile: {}", te.getBlockType().getTranslationKey());
                    MainRegistry.logger.warn(e.getMessage());
                } finally {
                    m.buf.release();
                }
            }

            return null;
        }
    }
}
