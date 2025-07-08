package com.hbm.tileentity;

import com.hbm.packet.NBTPacket;
import com.hbm.packet.PacketDispatcher;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

@Deprecated
public interface INBTPacketReceiver {

	@Deprecated
	public void networkUnpack(NBTTagCompound nbt);

	@Deprecated
	public static void networkPack(TileEntity that, NBTTagCompound data, int range) {
		BlockPos pos = that.getPos();
		PacketDispatcher.wrapper.sendToAllAround(new NBTPacket(data, pos), new TargetPoint(that.getWorld().provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}
}
