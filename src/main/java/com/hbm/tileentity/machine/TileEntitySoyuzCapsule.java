package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerSoyuzCapsule;
import com.hbm.inventory.gui.GUISoyuzCapsule;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityInventoryBase;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySoyuzCapsule extends TileEntityInventoryBase implements IGUIProvider {

	private static final AxisAlignedBB SOYUZ_CAPSULE_BOX = new AxisAlignedBB(-1, -1, -1, 2, 3, 2);
	
	public TileEntitySoyuzCapsule() {
		super(19);
	}

	@Override
	public String getName() {
		return "container.soyuzCapsule";
	}

	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return SOYUZ_CAPSULE_BOX.offset(pos);
    }

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerSoyuzCapsule(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUISoyuzCapsule(player.inventory, this);
	}
}
