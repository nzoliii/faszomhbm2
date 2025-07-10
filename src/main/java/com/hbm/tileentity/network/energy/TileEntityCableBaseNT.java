package com.hbm.tileentity.network.energy;

import com.hbm.api.energymk2.IEnergyConductorMK2;
import com.hbm.api.energymk2.Nodespace;
import com.hbm.lib.ForgeDirection;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityCableBaseNT extends TileEntity implements ITickable, IEnergyConductorMK2 {
	
	protected Nodespace.PowerNode node;

	@Override
	public void update() {

		if(!world.isRemote) {

			if(this.node == null || this.node.expired) {

				if(this.shouldCreateNode()) {
					this.node = Nodespace.getNode(world, pos);

					if(this.node == null || this.node.expired) {
						this.node = this.createNode();
						Nodespace.createNode(world, this.node);
					}
				}
			}
		}
	}

	public boolean canUpdate() {
		return (this.node == null || !this.node.hasValidNet()) && !this.isInvalid();
	}

	public boolean shouldCreateNode() {
		return true;
	}

	public void onNodeDestroyedCallback() {
		this.node = null;
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(!world.isRemote) {
			if(this.node != null) {
				Nodespace.destroyNode(world, pos);
			}
		}
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.UNKNOWN;
	}
}
