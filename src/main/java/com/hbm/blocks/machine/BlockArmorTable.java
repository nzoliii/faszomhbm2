package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerArmorTable;
import com.hbm.inventory.gui.GUIArmorTable;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockArmorTable extends Block implements IGUIProvider {

	public BlockArmorTable(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerArmorTable(player.inventory);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIArmorTable(player.inventory);
	}
}
