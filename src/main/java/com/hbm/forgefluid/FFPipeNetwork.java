package com.hbm.forgefluid;

import com.hbm.interfaces.IFluidPipe;
import com.hbm.main.MainRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class FFPipeNetwork implements IFluidHandler {

	
	protected Fluid type;
	protected List<ICapabilityProvider> fillables = new ArrayList<ICapabilityProvider>();
	protected List<IFluidPipe> pipes = new ArrayList<IFluidPipe>();
	
	protected FluidTank internalNetworkTank = new FluidTank(4000);

	private int tickTimer = 0;


	/**
	 * Constructs the network with a fluid type, hbm pipes only work with a single fluid pipe.
	 * @param fluid
	 */
	public FFPipeNetwork(Fluid fluid) {
		//new Exception().printStackTrace();
		this.type = fluid;
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
			MainRegistry.allPipeNetworks.add(this);
	}
	
	/**
	 * Gets the number of pipes and consumers in the network.
	 * @return - the number of pipes in the network plus the number of consumers
	 */
	public int getSize() {
		return pipes.size() + fillables.size();
	}

	/**
	 * Gets a list of pipes in the network.
	 * @return - list of pipes in the network
	 */
	public List<IFluidPipe> getPipes(){
		return this.pipes;
	}
	
	public World getNetworkWorld(){
		for(IFluidPipe pipe : this.pipes){
			if(pipe != null && ((TileEntity)pipe).getWorld() != null){
				return ((TileEntity)pipe).getWorld();
			}
		}
		return null;
	}
	
	/**
	 * Called whenever the world ticks to fill any connected fluid handlers
	 */
	public void updateTick(){
		//System.out.println(this.getType().getName() + " " + this.fillables.size());
		if(tickTimer < 20){
			tickTimer ++;
		} else {
			tickTimer = 0;
		}
		if(tickTimer == 9 || tickTimer == 19){
			if(pipes.isEmpty())
				this.destroySoft();
		//	cleanPipes();
			//cleanConsumers();
			fillFluidInit();
		}
		
	}
	
	public void fillFluidInit(){
		//if(getType() == Fluids.OIL.getFF();)
		//	System.out.println(this.fillables.size());
		//Pretty much the same thing as the transfer fluid in Library.java
		if(internalNetworkTank.getFluid() == null || internalNetworkTank.getFluidAmount() <= 0)
			return;
		
		List<IFluidHandler> consumers = new ArrayList<IFluidHandler>();
		for(ICapabilityProvider handle : this.fillables){
			if(handle != null && handle.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null) && handle.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).fill(new FluidStack(this.type, 1), false) > 0 && !consumers.contains(handle));
				consumers.add(handle.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null));
		}
		int size = consumers.size();
		if(size <= 0)
			return;
		int part = this.internalNetworkTank.getFluidAmount() / size;
		int lastPart = part + this.internalNetworkTank.getFluidAmount() - part * size;
		int i = 1;
		
		for(IFluidHandler consume : consumers){
			i++;
			if(internalNetworkTank.getFluid() != null && consume != null){
				internalNetworkTank.drain(consume.fill(new FluidStack(internalNetworkTank.getFluid().getFluid(), i<consumers.size()?part:lastPart), true), true);
			}
		}
	}

	/*public void cleanPipes(){
		for(IFluidPipe pipe : pipes){
			if(pipe == null)
				pipes.remove(pipe);
		}
	}
	
	public void cleanConsumers(){
		for(IFluidHandler consumer : fillables){
			if(consumer == null)
				fillables.remove(consumer);
		}
	}*/

	public void destroySoft() {
		this.fillables.clear();
		for(IFluidPipe pipe : pipes){
			pipe.setNetwork(null);
		}
		this.pipes.clear();
	}

	/**
	 * Sets the network fluid type, because that's how HBM pipes work. Also sets every pipe in the network to be this type.
	 * @param fluid - the fluid to set the network's fluid to
	 */
	public void setType(Fluid fluid) {
		//System.out.println("here");
		for(IFluidPipe pipe : this.pipes){
			pipe.setTypeTrue(fluid);
		}
		this.type = fluid;
	}

	/**
	 * Gets the network's fluid type.
	 * @return - the network's fluid type
	 */
	public Fluid getType() {
		return this.type;
	}

	/**
	 * Adds a pipe to the network. Used when doing stuff to all the network.
	 * @param pipe - the pipe to be added
	 * @return Whether it succeeded in adding the pipe.
	 */
	public boolean addPipe(IFluidPipe pipe) {
		if (pipe.getType() != null && pipe.getType() == this.getType()) {
			pipes.add(pipe);
			return pipes.add(pipe);
		}
		return false;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return this.internalNetworkTank.getTankProperties();
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource != null && resource.getFluid() == this.type){
			return internalNetworkTank.fill(resource, doFill);
			
		}else{
			return 0;
		}
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource != null && resource.getFluid() == this.type)
			return internalNetworkTank.drain(resource.amount, doDrain);
		else
			return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return internalNetworkTank.drain(maxDrain, doDrain);
	}

}
