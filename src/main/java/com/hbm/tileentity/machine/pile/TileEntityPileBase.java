package com.hbm.tileentity.machine.pile;

import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.PileNeutronHandler;
import com.hbm.render.util.GaugeUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class TileEntityPileBase extends TileEntity implements ITickable {

	@Override
	public abstract void update();

	@Override
	public void invalidate() {
		super.invalidate();
		NeutronNodeWorld.removeNode(world, this.getPos());
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		NeutronNodeWorld.removeNode(world, this.getPos());
	}

	protected void castRay(int flux) {

		BlockPos pos = this.getPos();

		if(flux == 0) {
			// simple way to remove the node from the cache when no flux is going into it!
			NeutronNodeWorld.removeNode(world, pos);
			return;
		}

		NeutronNodeWorld.StreamWorld streamWorld = NeutronNodeWorld.getOrAddWorld(world);
		PileNeutronHandler.PileNeutronNode node = (PileNeutronHandler.PileNeutronNode) streamWorld.getNode(pos);

		if(node == null) {
			node = PileNeutronHandler.makeNode(streamWorld, this);
			streamWorld.addNode(node);
		}

		Vec3d neutronVector = new Vec3d(1, 0, 0);
		neutronVector = GaugeUtil.rotateZ(neutronVector, (float)(Math.PI * 2D * world.rand.nextDouble()));
		neutronVector.rotateYaw((float)(Math.PI * 2D * world.rand.nextDouble()));
		neutronVector.rotatePitch((float)(Math.PI * 2D * world.rand.nextDouble()));

		new PileNeutronHandler.PileNeutronStream(node, neutronVector, flux);
	}
}
