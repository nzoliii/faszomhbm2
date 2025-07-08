package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityTowerLarge;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class MachineTowerLarge extends BlockDummyable implements ILookOverlay {

	public MachineTowerLarge(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		
		if(meta >= 12)
			return new TileEntityTowerLarge();
		
		if(meta >= 8)
			return new TileEntityProxyCombo(false, false, true);
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {12, 0, 4, 4, 4, 4};
	}

	@Override
	public int getOffset() {
		return 4;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;
		
		for(int i = 2; i <= 6; i++) {
			ForgeDirection dr2 = ForgeDirection.getOrientation(i);
			ForgeDirection rot = dr2.getRotation(ForgeDirection.UP);
			this.makeExtra(world, x + dr2.offsetX * 4, y, z + dr2.offsetZ * 4);
			this.makeExtra(world, x + dr2.offsetX * 4 + rot.offsetX * 3, y, z + dr2.offsetZ * 4 + rot.offsetZ * 3);
			this.makeExtra(world, x + dr2.offsetX * 4 + rot.offsetX * -3, y, z + dr2.offsetZ * 4 + rot.offsetZ * -3);
		}
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null)
			return;
		
		TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		
		if(!(te instanceof TileEntityTowerLarge))
			return;

		TileEntityTowerLarge tower = (TileEntityTowerLarge) te;

		List<String> text = new ArrayList<>();

		if(!tower.vacuumOptimised) {
			CBT_Atmosphere atmosphere = CelestialBody.getTrait(world, CBT_Atmosphere.class);
			if(CelestialBody.inOrbit(world) || atmosphere == null || atmosphere.getPressure() < 0.01) {
				text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! " + I18nUtil.resolveKey("atmosphere.noVacuum") + " ! ! !");
			}
		}

		for(int i = 0; i < tower.tanks.length; i++)
			text.add((i < 1 ? (TextFormatting.GREEN + "-> ") : (TextFormatting.RED + "<- ")) + TextFormatting.RESET + tower.tanks[i].getTankType().getLocalizedName() + ": " + tower.tanks[i].getFill() + "/" + tower.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}