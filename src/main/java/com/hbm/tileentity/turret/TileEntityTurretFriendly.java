package com.hbm.tileentity.turret;

import com.hbm.handler.BulletConfigSyncingUtil;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.gui.GUITurretFriendly;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityTurretFriendly extends TileEntityTurretChekhov implements IGUIProvider {

	static List<Integer> configs = new ArrayList<>();
	
	static {
		configs.add(BulletConfigSyncingUtil.R5_NORMAL);
		configs.add(BulletConfigSyncingUtil.R5_EXPLOSIVE);
		configs.add(BulletConfigSyncingUtil.R5_DU);
		configs.add(BulletConfigSyncingUtil.R5_STAR);
		configs.add(BulletConfigSyncingUtil.CHL_R5);
	}
	
	@Override
	protected List<Integer> getAmmoList() {
		return configs;
	}

	@Override
	public String getName() {
		return "container.turretFriendly";
	}

	@Override
	public int getDelay() {
		return 5;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretBase(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretFriendly(player.inventory, this);
	}
}
