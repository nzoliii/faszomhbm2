package com.hbm.tileentity;

import com.hbm.api.tile.ILoadedTile;
import com.hbm.sound.AudioWrapper;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

public class TileEntityLoadedBase extends TileEntity implements ILoadedTile, IBufPacketReceiver {
	
	public boolean isLoaded = true;
	public boolean muffled = false;
	
	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		this.isLoaded = false;
	}

	public AudioWrapper createAudioLoop() { return null; }

	public AudioWrapper rebootAudio(AudioWrapper wrapper) {
		wrapper.stopSound();
		AudioWrapper audio = createAudioLoop();
		audio.startSound();
		return audio;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.muffled = nbt.getBoolean("muffled");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("muffled", muffled);
		return super.writeToNBT(nbt);
	}

	public float getVolume(float baseVolume) {
		return muffled ? baseVolume * 0.1F : baseVolume;
	}
	private ByteBuf lastPackedBuf;

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(muffled);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.muffled = buf.readBoolean();
	}

//	/** Sends a sync packet that uses ByteBuf for efficient information-cramming */
//	public void networkPackNT(int range) {
//		if(world.isRemote) return;
//
//		BufPacket packet = new BufPacket(xCoord, yCoord, zCoord, this);
//		ByteBuf preBuf = packet.getPreBuf();
//
//		// Don't send unnecessary packets, except for maybe one every second or so.
//		// If we stop sending duplicate packets entirely, this causes issues when
//		// a client unloads and then loads back a chunk with an unchanged tile entity.
//		// For that client, the tile entity will appear default until anything changes about it.
//		// In my testing, this can be reliably reproduced with a full fluid barrel, for instance.
//		// I think it might be fixable by doing something with getDescriptionPacket() and onDataPacket(),
//		// but this sidesteps the problem for the mean time.
//		if(preBuf.equals(lastPackedBuf) && this.world.getTotalWorldTime() % 20 != 0) return;
//
//		this.lastPackedBuf = preBuf.copy();
//
//		PacketThreading.createAllAroundThreadedPacket(packet, new NetworkRegistry.TargetPoint(this.world.provider.dimensionId, xCoord, yCoord, zCoord, range));
//	}
}
