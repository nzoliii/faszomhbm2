package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.util.I18nUtil;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityTowerSmall;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class MachineTowerSmall extends BlockDummyable implements ILookOverlay {

	public MachineTowerSmall(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int meta) {
		
		if(meta >= 12)
			return new TileEntityTowerSmall();
		
		if(meta >= 8)
			return new TileEntityProxyCombo(false, false, true);
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {18, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;
		
		for(int i = 2; i <= 6; i++) {
			ForgeDirection dr2 = ForgeDirection.getOrientation(i);
			this.makeExtra(world, x + dr2.offsetX * 2, y, z + dr2.offsetZ * 2);
		}
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		
		if(pos == null)
			return;
		
		TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		
		if(!(te instanceof TileEntityTowerSmall))
			return;
		
		TileEntityTowerSmall chungus = (TileEntityTowerSmall) te;
		
		List<String> text = new ArrayList();

		text.add("§a-> §r" + ModForgeFluids.SPENTSTEAM.getLocalizedName(new FluidStack(ModForgeFluids.SPENTSTEAM, 1)) + ": " + chungus.tanks[0].getFluidAmount() + "/" + chungus.tanks[0].getCapacity() + "mB");
		text.add("§c<- §r" + FluidRegistry.WATER.getLocalizedName(new FluidStack(FluidRegistry.WATER, 1)) + ": " + chungus.tanks[1].getFluidAmount() + "/" + chungus.tanks[1].getCapacity() + "mB");

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}