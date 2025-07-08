package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityDeuteriumTower extends TileEntityDeuteriumExtractor {

	public TileEntityDeuteriumTower() {
		super();
		tanks[0] = new FluidTankNTM(Fluids.WATER, 50000);
		tanks[1] = new FluidTankNTM(Fluids.HEAVYWATER, 5000);
	}

	@Override
	protected void updateConnections() {

		for(DirPos pos : getConPos()) {
			this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}


	private DirPos[] getConPos() {
		
		ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);

		return new DirPos[] {
				new DirPos(pos.add(-dir.offsetX * 2, 0, -dir.offsetZ * 2), dir.getOpposite()),
				new DirPos(pos.add(-dir.offsetX * 2 + rot.offsetX, 0, -dir.offsetZ * 2 + rot.offsetZ), dir.getOpposite()),
				
				new DirPos(pos.add(dir.offsetX, 0, dir.offsetZ), dir),
				new DirPos(pos.add(dir.offsetX + rot.offsetX, 0, dir.offsetZ  + rot.offsetZ), dir),
				
				new DirPos(pos.add(-rot.offsetX, 0, -rot.offsetZ), rot.getOpposite()),
				new DirPos(pos.add(-dir.offsetX - rot.offsetX, 0, -dir.offsetZ - rot.offsetZ), rot.getOpposite()),
				
				new DirPos(pos.add(rot.offsetX * 2, 0, rot.offsetZ * 2), rot),
				new DirPos(pos.add(dir.offsetX + rot.offsetX * 2, 0, -dir.offsetZ + rot.offsetZ * 2), rot)
		};
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 10,
					pos.getZ() + 2
					);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public long getMaxPower() {
		return 100_000;
	}
}