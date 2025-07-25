package com.hbm.tileentity.machine.rbmk;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerRBMKControlAuto;
import com.hbm.inventory.gui.GUIRBMKControlAuto;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKControlManual.RBMKColor;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class TileEntityRBMKControlAuto extends TileEntityRBMKControl implements IControlReceiver, IGUIProvider {
	
	public RBMKFunction function = RBMKFunction.LINEAR;
	public double levelLower;
	public double levelUpper;
	public double heatLower;
	public double heatUpper;

	@Override
	public String getName() {
		return "container.rbmkControlAuto";
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
	}
	
	@Override
	public void update() {
		
		if(!world.isRemote) {
			
			double fauxLevel = 0;

			double lowerBound = Math.min(this.heatLower, this.heatUpper);
			double upperBound = Math.max(this.heatLower, this.heatUpper);
			
			if(this.heat < lowerBound) {
				fauxLevel = this.levelLower;
				
			} else if(this.heat > upperBound) {
				fauxLevel = this.levelUpper;
				
			} else {
	
				switch(this.function) {
				case LINEAR:
					// my brain hasn't been this challenged since my math finals in
					// '19
					fauxLevel = (this.heat - this.heatLower) * ((this.levelUpper - this.levelLower) / (this.heatUpper - this.heatLower)) + this.levelLower;
					break;
	
				case QUAD_UP:
					// so this is how we roll, huh?
					fauxLevel = Math.pow((this.heat - this.heatLower) / (this.heatUpper - this.heatLower), 2) * (this.levelUpper - this.levelLower) + this.levelLower;
					break;
				case QUAD_DOWN:
					// sometimes my genius is almost frightening
					fauxLevel = Math.pow((this.heat - this.heatUpper) / (this.heatLower - this.heatUpper), 2) * (this.levelLower - this.levelUpper) + this.levelUpper;
					break;
				}
			}
			
			this.targetLevel = fauxLevel * 0.01D;
			this.targetLevel = MathHelper.clamp(this.targetLevel, 0D, 1D);
		}
		
		super.update();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.levelLower = nbt.getDouble("levelLower");
		this.levelUpper = nbt.getDouble("levelUpper");
		this.heatLower = nbt.getDouble("heatLower");
		this.heatUpper = nbt.getDouble("heatUpper");
		
		if(nbt.hasKey("function"))
			this.function = RBMKFunction.values()[nbt.getInteger("function")];
		else
			this.function = null;
	}
	
	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setDouble("levelLower", levelLower);
		nbt.setDouble("levelUpper", levelUpper);
		nbt.setDouble("heatLower", heatLower);
		nbt.setDouble("heatUpper", heatUpper);
		
		if(function != null)
			nbt.setInteger("function", function.ordinal());
		
		return nbt;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		
		if(data.hasKey("function")) {
			int c = Math.abs(data.getInteger("function")) % RBMKColor.values().length;
			this.function = RBMKFunction.values()[c];
			
		} else {

			this.levelLower = data.getDouble("levelLower");
			this.levelUpper = data.getDouble("levelUpper");
			this.heatLower = data.getDouble("heatLower");
			this.heatUpper = data.getDouble("heatUpper");
		}
		
		this.markDirty();
	}
	
	public static enum RBMKFunction {
		LINEAR,
		QUAD_UP,
		QUAD_DOWN
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.CONTROL_AUTO;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerRBMKControlAuto(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIRBMKControlAuto(player.inventory, this);
	}
}