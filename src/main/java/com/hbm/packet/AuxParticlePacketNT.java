package com.hbm.packet;

import java.io.IOException;

import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.ThreadedPacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AuxParticlePacketNT extends ThreadedPacket {

	private NBTTagCompound nbt;

	public AuxParticlePacketNT() { }

	public AuxParticlePacketNT(NBTTagCompound nbt, double x, double y, double z) {
		this.nbt = nbt;
		this.nbt.setDouble("posX", x);
		this.nbt.setDouble("posY", y);
		this.nbt.setDouble("posZ", z);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {
			this.nbt = pbuf.readCompoundTag();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		if (this.nbt != null) {
			PacketBuffer pbuf = new PacketBuffer(buf);
			pbuf.writeCompoundTag(this.nbt);
		}
	}

	public static class Handler implements IMessageHandler<AuxParticlePacketNT, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(AuxParticlePacketNT m, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				if(Minecraft.getMinecraft().world == null)
					return;

				if(m.nbt != null) {
					MainRegistry.proxy.effectNT(m.nbt);
				}
			});

			return null;
		}
	}
}