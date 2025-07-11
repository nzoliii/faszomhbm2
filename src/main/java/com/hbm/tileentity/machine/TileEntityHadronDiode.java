package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.BlockHadronDiode;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.lib.ForgeDirection;
import com.hbm.packet.BufPacket;
import com.hbm.tileentity.TileEntityTickingBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

public class TileEntityHadronDiode extends TileEntityTickingBase {

	int age = 0;
	boolean fatherIAskOfYouToUpdateMe = false;

	public DiodeConfig[] sides = new DiodeConfig[6];
	
	public TileEntityHadronDiode() {
		for(int i = 0; i < 6; i ++){
			sides[i] = DiodeConfig.NONE;
		}
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			age++;

			if(age >= 20) {
				age = 0;
				sendSides();
			}
			
			if(fatherIAskOfYouToUpdateMe) {
				fatherIAskOfYouToUpdateMe = false;
				//world.markBlockRangeForRenderUpdate(pos, pos);
				BlockHadronDiode.resetBlockState(world, pos);
			}
		}
	}

	@Override
	public String getInventoryName() {
		return "";
	}
	
	public void sendSides() {
		BlockHadronDiode.resetBlockState(world, pos);
		networkPackNT(250);
	}

	@Override
	public void serialize(ByteBuf buf) {
		for(int i = 0; i < 6; i++) {
			if(sides[i] != null)
				buf.writeInt(sides[i].ordinal());
		}
	}
	
	@Override
	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < 6; i++) {
			sides[i] = DiodeConfig.values()[buf.readInt()];
		}
		//world.markBlockRangeForRenderUpdate(pos, pos);
	}

	public void networkPackNT(int range) {
		if (!world.isRemote)
			PacketThreading.createAllAroundThreadedPacket(new BufPacket(pos.getX(), pos.getY(), pos.getZ(), this), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), range));
	}
	
	public DiodeConfig getConfig(int side) {

		if(ForgeDirection.getOrientation(side) == ForgeDirection.UNKNOWN)
			return DiodeConfig.NONE;

		DiodeConfig conf = sides[side];

		if(conf == null)
			return DiodeConfig.NONE;

		return conf;
	}

	public void setConfig(int side, int config) {
		sides[side] = DiodeConfig.values()[config];
		this.markDirty();
		sendSides();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for(int i = 0; i < 6; i++) {
			sides[i] = DiodeConfig.values()[compound.getInteger("side_" + i)];
		}

		fatherIAskOfYouToUpdateMe = true;
		super.readFromNBT(compound);
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
		for(int i = 0; i < 6; i++) {

			if(sides[i] != null) {
				compound.setInteger("side_" + i, sides[i].ordinal());
			}
		}
		return super.writeToNBT(compound);
	}

	public static enum DiodeConfig {
		NONE,
		IN,
		OUT
	}
	
}
