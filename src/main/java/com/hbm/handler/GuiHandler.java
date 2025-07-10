package com.hbm.handler;

import com.hbm.interfaces.Spaghetti;
import com.hbm.tileentity.IGUIProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

@Deprecated //Oh no...
@Spaghetti("ew")
public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		
		TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
		if(entity instanceof IGUIProvider) {
			return ((IGUIProvider) entity).provideContainer(ID, player, world, x, y, z);
		}

		Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		
		if(b instanceof IGUIProvider) {
			return ((IGUIProvider) b).provideContainer(ID, player, world, x, y, z);
		}
		
		ItemStack item = player.getHeldItemMainhand();
		
		if(!item.isEmpty() && item.getItem() instanceof IGUIProvider) {
			return ((IGUIProvider) item.getItem()).provideContainer(ID, player, world, x, y, z);
		}
		
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));

		if(entity instanceof IGUIProvider) {
			return ((IGUIProvider) entity).provideGUI(ID, player, world, x, y, z);
		}
		
		Block b = world.getBlockState(new BlockPos(x, y, z)).getBlock();
		
		if(b instanceof IGUIProvider) {
			return ((IGUIProvider) b).provideGUI(ID, player, world, x, y, z);
		}
		
		ItemStack item = player.getHeldItemMainhand();
		
		if(!item.isEmpty() && item.getItem() instanceof IGUIProvider) {
			return ((IGUIProvider) item.getItem()).provideGUI(ID, player, world, x, y, z);
		}
		
		return null;
	}

}
