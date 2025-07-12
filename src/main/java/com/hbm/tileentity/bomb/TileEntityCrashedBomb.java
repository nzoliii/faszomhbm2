package com.hbm.tileentity.bomb;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class TileEntityCrashedBomb extends TileEntity {

	private int dudChoice = -1;

	public int getDudChoice() {
		return dudChoice;
	}

	public void setDudChoice(int choice) {
		this.dudChoice = choice;
		this.markDirty();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("DudChoice")) {
			this.dudChoice = compound.getInteger("DudChoice");
		} else {
			Random rand = new Random(this.getPos().toLong());
			this.dudChoice = rand.nextInt(5);
			this.markDirty();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("DudChoice", this.dudChoice);
		return compound;
	}
}