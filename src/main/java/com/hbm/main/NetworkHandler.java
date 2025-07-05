package com.hbm.main;

import com.hbm.handler.threading.PacketThreading;
import com.hbm.packet.threading.ThreadedPacket;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleChannelHandlerWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.List;

import static net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec.INBOUNDPACKETTRACKER;

// Essentially the `SimpleNetworkWrapper` from FML but doesn't flush the packets immediately. Also now with a custom codec!
public class NetworkHandler {

    // Network codec for allowing packets to be "precompiled".
    @ChannelHandler.Sharable
    private static class PrecompilingNetworkCodec extends MessageToMessageCodec<FMLProxyPacket, Object> {

        private final TByteObjectHashMap<Class<? extends IMessage>> discriminators = new TByteObjectHashMap<>();
        private final TObjectByteHashMap<Class<? extends IMessage>> types = new TObjectByteHashMap<>();

        public void addDiscriminator(int discriminator, Class<? extends IMessage> type) {
            discriminators.put((byte) discriminator, type);
            types.put(type, (byte) discriminator);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            ctx.attr(INBOUNDPACKETTRACKER).set(new ThreadLocal<>());
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) {
            ByteBuf outboundBuf = PooledByteBufAllocator.DEFAULT.heapBuffer();
            byte discriminator;
            Class<?> msgClass = msg.getClass();
            discriminator = types.get(msgClass);
            outboundBuf.writeByte(discriminator);

            if(msg instanceof ThreadedPacket) // Precompiled packet to avoid race conditions/speed up serialization.
                outboundBuf.writeBytes(((ThreadedPacket) msg).getCompiledBuffer());
            else if(msg instanceof IMessage)
                ((IMessage) msg).toBytes(outboundBuf);
            else
                throw new CodecException("Unknown packet codec requested during encoding, expected IMessage/PrecompiledPacket, got " + msg.getClass().getName());

            FMLProxyPacket proxy = new FMLProxyPacket(new PacketBuffer(Unpooled.buffer().writeBytes(outboundBuf)), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());            outboundBuf.release();
            WeakReference<FMLProxyPacket> ref = ctx.attr(INBOUNDPACKETTRACKER).get().get();
            FMLProxyPacket old = ref == null ? null : ref.get();
            if (old != null) {
                proxy.setDispatcher(old.getDispatcher());
            }
            out.add(proxy);
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
            ByteBuf inboundBuf = msg.payload();
            byte discriminator = inboundBuf.readByte();
            Class<?> originalMsgClass = discriminators.get(discriminator);

            if(originalMsgClass == null)
                throw new CodecException("Undefined message for discriminator " + discriminator + " in channel " + msg.channel());

            Object newMsg = originalMsgClass.getDeclaredConstructor().newInstance();
            ctx.attr(INBOUNDPACKETTRACKER).get().set(new WeakReference<>(msg));

            if(newMsg instanceof IMessage) // pretty much always the case
                ((IMessage) newMsg).fromBytes(inboundBuf.slice());
            else
                throw new CodecException("Unknown packet codec requested during decoding, expected IMessage/PrecompiledPacket, got " + msg.getClass().getName());

            out.add(newMsg);
        }
    }

    private static FMLEmbeddedChannel clientChannel;
    private static FMLEmbeddedChannel serverChannel;

    private static PrecompilingNetworkCodec packetCodec;

    public NetworkHandler(String name) {
        packetCodec = new PrecompilingNetworkCodec();
        EnumMap<Side, FMLEmbeddedChannel> channels = NetworkRegistry.INSTANCE.newChannel(name, packetCodec);
        clientChannel = channels.get(Side.CLIENT);
        serverChannel = channels.get(Side.SERVER);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler) {
        try {
            return handler.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {
        registerMessage(instantiate(messageHandler), requestMessageType, discriminator, side);
    }

    public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType, int discriminator, Side side) {
        packetCodec.addDiscriminator(discriminator, requestMessageType);

        FMLEmbeddedChannel channel = side.isClient() ? clientChannel : serverChannel;
        String type = channel.findChannelHandlerNameForType(PrecompilingNetworkCodec.class);

        SimpleChannelHandlerWrapper<REQ, REPLY> handler = new SimpleChannelHandlerWrapper<>(messageHandler, side, requestMessageType);
        channel.pipeline().addAfter(type, messageHandler.getClass().getName(), handler);
    }

    public Packet<?> getPacketFrom(IMessage message) {
        return serverChannel.generatePacketFrom(message);
    }

    public static void flush() {
        clientChannel.flush();
        serverChannel.flush();
    }

    public void sendToServer(IMessage message) { // No thread protection needed here, since the client never threads packets to the server.
        clientChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        clientChannel.write(message);
    }

    public void sendToDimension(IMessage message, int dimensionId) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendToAllAround(ByteBuf message, NetworkRegistry.TargetPoint point) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendToAllTracking(IMessage message, NetworkRegistry.TargetPoint point) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TRACKING_POINT);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendToAllTracking(IMessage message, Entity entity) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TRACKING_ENTITY);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entity);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendTo(IMessage message, EntityPlayerMP player) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }

    public void sendToAll(IMessage message) {
        try {
            PacketThreading.lock.lock();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
            serverChannel.write(message);
        } finally {
            PacketThreading.lock.unlock();
        }
    }
}