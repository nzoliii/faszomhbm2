package com.hbm.packet.threading;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * This is the base class for any packets passing through the PacketThreading system.
 */
public abstract class ThreadedPacket implements IMessage {

    ByteBuf compiledBuffer;

    public void compile() {
        if(compiledBuffer != null)
            compiledBuffer.release();

        compiledBuffer = Unpooled.buffer();

        this.toBytes(compiledBuffer); // Create buffer and read data to it.
    }

    /**
     * Returns the compiled buffer.
     */
    public synchronized ByteBuf getCompiledBuffer() {
        if(compiledBuffer == null || compiledBuffer.readableBytes() <= 0 /* No data written */)
            this.compile();
        return compiledBuffer;
    }
}
