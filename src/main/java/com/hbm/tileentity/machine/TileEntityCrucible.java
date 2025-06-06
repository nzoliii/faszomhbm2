package com.hbm.tileentity.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.CombinationRecipes;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.inventory.container.ContainerCrucible;
import com.hbm.inventory.gui.GUICrucible;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.CrucibleRecipes;
import com.hbm.inventory.CrucibleRecipes.CrucibleRecipe;
import com.hbm.lib.ForgeDirection;
import com.hbm.items.ModItems;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.IConfigurableMachine;
import com.hbm.util.CrucibleUtil;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.tile.IHeatSource;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class TileEntityCrucible extends TileEntityMachineBase implements ITickable, IGUIProvider, ICrucibleAcceptor, IConfigurableMachine {

	public int heat;
	public int progress;
	
	public List<MaterialStack> recipeStack = new ArrayList();
	public List<MaterialStack> wasteStack = new ArrayList();

	/* CONFIGURABLE CONSTANTS */
	//because eclipse's auto complete is dumb as a fucking rock, it's now called "ZCapacity" so it's listed AFTER the actual stacks in the auto complete list.
	//also martin i know you read these: no i will not switch to intellij after using eclipse for 8 years.
	public static int recipeZCapacity = MaterialShapes.BLOCK.q(16);
	public static int wasteZCapacity = MaterialShapes.BLOCK.q(16);
	public static int processTime = 20_000;
	public static double diffusion = 0.25D;
	public static int maxHeat = 100_000;

	public TileEntityCrucible() {
		super(10, 1);
	}

	@Override
	public String getName() {
		return "container.machineCrucible";
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			tryPullHeat();
			
			/* collect items */
			if(world.getTotalWorldTime() % 5 == 0) {
				List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY() + 0.5, pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 1, pos.getZ() + 1.5));
				EntityItem itemE = null;
				for(int i = 1; i < 10; i++) {
					if(itemE == null){
						for(EntityItem entity : list) {
							if(this.isItemSmeltable(entity.getItem())){
								itemE = entity; 
								break;
							}
						}
					}
					if(itemE == null) break;
					if(inventory.getStackInSlot(i).isEmpty()) {
						
						ItemStack stack = itemE.getItem();
						if(stack.getCount() == 1) {
							inventory.setStackInSlot(i, stack.copy());
							list.remove(itemE);
							itemE.setDead();
							break;
						} else {
							ItemStack cStack = stack.copy();
							cStack.setCount(1);
							inventory.setStackInSlot(i, cStack);
							stack.shrink(1);
						}
						
						this.markDirty();
					}
				}
			}

			int totalCap = recipeZCapacity + wasteZCapacity;
			int totalMass = 0;

			for(MaterialStack stack : recipeStack) totalMass += stack.amount;
			for(MaterialStack stack : wasteStack) totalMass += stack.amount;
			
			double level = ((double) totalMass / (double) totalCap) * 0.875D;
			
			List<EntityLivingBase> living = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5 + level, pos.getZ() + 0.5).expand(1, 0, 1));
			for(EntityLivingBase entity : living) {
				entity.attackEntityFrom(DamageSource.LAVA, 5F);
				entity.setFire(5);
			}
			
			/* smelt items from buffer */
			if(!trySmelt()) {
				this.progress = 0;
			}
			
			tryRecipe();
			
			/* pour waste stack */
			if(!this.wasteStack.isEmpty()) {
				
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
				Vec3 impact = Vec3.createVectorHelper(0, 0, 0);
				MaterialStack didPour = CrucibleUtil.pourFullStack(world, pos.getX() + 0.5D + dir.offsetX * 1.875D, pos.getY() + 0.25D, pos.getZ() + 0.5D + dir.offsetZ * 1.875D, 6, true, this.wasteStack, MaterialShapes.NUGGET.q(3), impact);
				
				if(didPour != null) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", didPour.material.moltenColor);
					data.setByte("dir", (byte) dir.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(impact.yCoord) - 0.875)));
					
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.getX() + 0.5D + dir.offsetX * 1.875D, pos.getY(), pos.getZ() + 0.5D + dir.offsetZ * 1.875D), new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));
				}
			}
			
			/* pour recipe stack */
			if(!this.recipeStack.isEmpty()) {
				
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				List<MaterialStack> toCast = new ArrayList();
				
				CrucibleRecipe recipe = this.getLoadedRecipe();
				//if no recipe is loaded, everything from the recipe stack will be drainable
				if(recipe == null) {
					toCast.addAll(this.recipeStack);
				} else {
					
					for(MaterialStack stack : this.recipeStack) {
						for(MaterialStack output : recipe.output) {
							if(stack.material == output.material) {
								toCast.add(stack);
								break;
							}
						}
					}
				}
				
				Vec3 impact = Vec3.createVectorHelper(0, 0, 0);
				MaterialStack didPour = CrucibleUtil.pourFullStack(world, pos.getX() + 0.5D + dir.offsetX * 1.875D, pos.getY() + 0.25D, pos.getZ() + 0.5D + dir.offsetZ * 1.875D, 6, true, toCast, MaterialShapes.NUGGET.q(3), impact);

				if(didPour != null) {
					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "foundry");
					data.setInteger("color", didPour.material.moltenColor);
					data.setByte("dir", (byte) dir.ordinal());
					data.setFloat("off", 0.625F);
					data.setFloat("base", 0.625F);
					data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(impact.yCoord) - 0.875)));
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, pos.getX() + 0.5D + dir.offsetX * 1.875D, pos.getY(), pos.getZ() + 0.5D + dir.offsetZ * 1.875D), new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));
				
				}
			}

			/* clean up stacks */
			this.recipeStack.removeIf(o -> o.amount <= 0);
			this.wasteStack.removeIf(x -> x.amount <= 0);
			
			/* sync */
			NBTTagCompound data = new NBTTagCompound();
			int[] rec = new int[recipeStack.size() * 2];
			int[] was = new int[wasteStack.size() * 2];
			for(int i = 0; i < recipeStack.size(); i++) { MaterialStack sta = recipeStack.get(i); rec[i * 2] = sta.material.id; rec[i * 2 + 1] = sta.amount; }
			for(int i = 0; i < wasteStack.size(); i++) { MaterialStack sta = wasteStack.get(i); was[i * 2] = sta.material.id; was[i * 2 + 1] = sta.amount; }
			data.setIntArray("rec", rec);
			data.setIntArray("was", was);
			data.setInteger("progress", progress);
			data.setInteger("heat", heat);
			this.networkPack(data, 25);
		}
	}

	@Override
	public void networkUnpack(NBTTagCompound nbt) {

		this.recipeStack.clear();
		this.wasteStack.clear();
		
		int[] rec = nbt.getIntArray("rec");
		for(int i = 0; i < rec.length>>1; i++) {
			recipeStack.add(new MaterialStack(Mats.matById.get(rec[i * 2]), rec[i * 2 + 1]));
		}
		
		int[] was = nbt.getIntArray("was");
		for(int i = 0; i < was.length>>1; i++) {
			wasteStack.add(new MaterialStack(Mats.matById.get(was[i * 2]), was[i * 2 + 1]));
		}
		
		this.progress = nbt.getInteger("progress");
		this.heat = nbt.getInteger("heat");
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int[] rec = nbt.getIntArray("rec");
		for(int i = 0; i < rec.length>>1; i++) {
			recipeStack.add(new MaterialStack(Mats.matById.get(rec[i * 2]), rec[i * 2 + 1]));
		}
		
		int[] was = nbt.getIntArray("was");
		for(int i = 0; i < was.length>>1; i++) {
			wasteStack.add(new MaterialStack(Mats.matById.get(was[i * 2]), was[i * 2 + 1]));
		}
		
		this.progress = nbt.getInteger("progress");
		this.heat = nbt.getInteger("heat");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		
		int[] rec = new int[recipeStack.size() * 2];
		int[] was = new int[wasteStack.size() * 2];
		for(int i = 0; i < recipeStack.size(); i++) { MaterialStack sta = recipeStack.get(i); rec[i * 2] = sta.material.id; rec[i * 2 + 1] = sta.amount; }
		for(int i = 0; i < wasteStack.size(); i++) { MaterialStack sta = wasteStack.get(i); was[i * 2] = sta.material.id; was[i * 2 + 1] = sta.amount; }
		nbt.setIntArray("rec", rec);
		nbt.setIntArray("was", was);
		nbt.setInteger("progress", progress);
		nbt.setInteger("heat", heat);
		return super.writeToNBT(nbt);
	}
	
	protected void tryPullHeat() {
		
		if(this.heat >= this.maxHeat) return;
		
		TileEntity con = world.getTileEntity(pos.down());
		
		if(con instanceof IHeatSource) {
			IHeatSource source = (IHeatSource) con;
			int diff = source.getHeatStored() - this.heat;
			
			if(diff == 0) {
				return;
			}
			
			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > this.maxHeat)
					this.heat = this.maxHeat;
				return;
			}
		}
		
		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}
	
	protected boolean trySmelt() {
		
		if(this.heat < maxHeat>>1) return false;
		
		int slot = this.getFirstSmeltableSlot();
		if(slot == -1) return false;
		
		int delta = this.heat - (maxHeat / 2);
		delta *= 0.05;
		
		this.progress += delta;
		this.heat -= delta;
		
		if(this.progress >= processTime) {
			this.progress = 0;
			
			List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(inventory.getStackInSlot(slot));
			CrucibleRecipe recipe = getLoadedRecipe();
			
			for(MaterialStack material : materials) {
				boolean mainStack = recipe != null && (getQuantaFromType(recipe.input, material.material) > 0 || getQuantaFromType(recipe.output, material.material) > 0);
				
				if(mainStack) {
					this.addToStack(this.recipeStack, material);
				} else {
					this.addToStack(this.wasteStack, material);
				}
			}
			
			inventory.getStackInSlot(slot).shrink(1);
		}
		
		return true;
	}
	
	protected void tryRecipe() {
		CrucibleRecipe recipe = this.getLoadedRecipe();
		
		if(recipe == null) return;
		if(world.getTotalWorldTime() % recipe.frequency > 0) return;
		
		for(MaterialStack stack : recipe.input) {
			if(getQuantaFromType(this.recipeStack, stack.material) < stack.amount) return;
		}
		
		for(MaterialStack stack : this.recipeStack) {
			stack.amount -= getQuantaFromType(recipe.input, stack.material);
		}
		
		outer:
		for(MaterialStack out : recipe.output) {
			
			for(MaterialStack stack : this.recipeStack) {
				if(stack.material == out.material) {
					stack.amount += out.amount;
					continue outer;
				}
			}
			
			this.recipeStack.add(out.copy());
		}
	}
	
	protected int getFirstSmeltableSlot() {
		
		for(int i = 1; i < 10; i++) {
			
			ItemStack stack = inventory.getStackInSlot(i);
			
			if(stack != null && isItemSmeltable(stack)) {
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		
		if(i == 0) {
			return stack.getItem() == ModItems.crucible_template;
		}
		
		return isItemSmeltable(stack);
	}
	
	public boolean isItemSmeltable(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return false;
		List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(stack);
		
		//if there's no materials in there at all, don't smelt
		if(materials.isEmpty())
			return false;
		
		CrucibleRecipe recipe = getLoadedRecipe();
		
		//needs to be true, will always be true if there's no recipe loaded
		boolean matchesRecipe = recipe == null;
		
		//the amount of material in the entire recipe input
		int recipeContent = recipe != null ? recipe.getInputAmount() : 0;
		//the total amount of the current waste stack, used for simulation
		int recipeAmount = getQuantaFromType(this.recipeStack, null);
		int wasteAmount = getQuantaFromType(this.wasteStack, null);
		
		for(MaterialStack mat : materials) {
			//if no recipe is loaded, everything will land in the waste stack
			int recipeInputRequired = recipe != null ? getQuantaFromType(recipe.input, mat.material) : 0;
			
			//this allows pouring the output material back into the crucible
			if(recipe != null && getQuantaFromType(recipe.output, mat.material) > 0) {
				recipeAmount += mat.amount;
				matchesRecipe = true;
				continue;
			}
			
			if(recipeInputRequired == 0) {
				//if this type isn't required by the recipe, add it to the waste stack
				wasteAmount += mat.amount;
			} else {
				
				//the maximum is the recipe's ratio scaled up to the recipe stack's capacity
				int matMaximum = recipeInputRequired * this.recipeZCapacity / recipeContent;
				int amountStored = getQuantaFromType(recipeStack, mat.material);
				
				matchesRecipe = true;
				
				recipeAmount += mat.amount;
				
				//if the amount of that input would exceed the amount dictated by the recipe, return false
				if(recipe != null && amountStored + mat.amount > matMaximum)
					return false;
			}
		}
		
		//if the amount doesn't exceed the capacity, return true
		return recipeAmount <= this.recipeZCapacity && wasteAmount <= this.wasteZCapacity;
	}
	
	public void addToStack(List<MaterialStack> stack, MaterialStack matStack) {
		
		for(MaterialStack mat : stack) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}
		
		stack.add(matStack.copy());
	}
	
	public CrucibleRecipe getLoadedRecipe() {
		
		if(inventory.getStackInSlot(0) != null && inventory.getStackInSlot(0).getItem() == ModItems.crucible_template) {
			return CrucibleRecipes.recipes.get(inventory.getStackInSlot(0).getItemDamage());
		}
		
		return null;
	}
	
	/* "Arrays and Lists don't have a common ancestor" my fucking ass */
	public int getQuantaFromType(MaterialStack[] stacks, NTMMaterial mat) {
		for(MaterialStack stack : stacks) {
			if(mat == null || stack.material == mat) {
				return stack.amount;
			}
		}
		return 0;
	}
	
	public int getQuantaFromType(List<MaterialStack> stacks, NTMMaterial mat) {
		int sum = 0;
		for(MaterialStack stack : stacks) {
			if(stack.material == mat) {
				return stack.amount;
			}
			if(mat == null) {
				sum += stack.amount;
			}
		}
		return sum;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e) {
		return new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerCrucible(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUICrucible(player.inventory, this);
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 1,
					pos.getY(),
					pos.getZ() - 1,
					pos.getX() + 2,
					pos.getY() + 2,
					pos.getZ() + 2
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	public boolean canAcceptPartialPour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		
		CrucibleRecipe recipe = getLoadedRecipe();
		
		if(recipe == null) {
			return getQuantaFromType(this.wasteStack, null) < this.wasteZCapacity;
		}
		
		int recipeContent = recipe.getInputAmount();
		int recipeInputRequired = getQuantaFromType(recipe.input, stack.material);
		int matMaximum = recipeInputRequired * this.recipeZCapacity / recipeContent;
		int amountStored = getQuantaFromType(recipeStack, stack.material);
		
		return amountStored < matMaximum && getQuantaFromType(this.recipeStack, null) < this.recipeZCapacity;
	}

	@Override
	public MaterialStack pour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		
		CrucibleRecipe recipe = getLoadedRecipe();
		
		if(recipe == null) {
			
			int amount = getQuantaFromType(this.wasteStack, null);
			
			if(amount + stack.amount <= this.wasteZCapacity) {
				this.addToStack(this.wasteStack, stack.copy());
				return null;
			} else {
				int toAdd = this.wasteZCapacity - amount;
				this.addToStack(this.wasteStack, new MaterialStack(stack.material, toAdd));
				return new MaterialStack(stack.material, stack.amount - toAdd);
			}
		}
		
		int recipeContent = recipe.getInputAmount();
		int recipeInputRequired = getQuantaFromType(recipe.input, stack.material);
		int matMaximum = recipeInputRequired * this.recipeZCapacity / recipeContent;
		
		if(recipeInputRequired + stack.amount <= matMaximum) {
			this.addToStack(this.recipeStack, stack.copy());
			return null;
		}
		
		int toAdd = matMaximum - stack.amount;
		toAdd = Math.min(toAdd, this.recipeZCapacity - getQuantaFromType(this.recipeStack, null));
		this.addToStack(this.recipeStack, new MaterialStack(stack.material, toAdd));
		return new MaterialStack(stack.material, stack.amount - toAdd);
	}

	@Override public boolean canAcceptPartialFlow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack flow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) { return null; }


	@Override
	public String getConfigName() {
		return "crucible";
	}

	@Override
	public void readIfPresent(JsonObject obj) {
		recipeZCapacity = IConfigurableMachine.grab(obj, "I:recipeCapacity", recipeZCapacity);
		wasteZCapacity = IConfigurableMachine.grab(obj, "I:wasteCapacity", wasteZCapacity);
		processTime = IConfigurableMachine.grab(obj, "I:processHeat", processTime);
		diffusion = IConfigurableMachine.grab(obj, "D:diffusion", diffusion);
		maxHeat = IConfigurableMachine.grab(obj, "I:heatCap", maxHeat);
	}

	@Override
	public void writeConfig(JsonWriter writer) throws IOException {
		writer.name("I:recipeCapacity").value(recipeZCapacity);
		writer.name("I:wasteCapacity").value(wasteZCapacity);
		writer.name("I:processHeat").value(processTime);
		writer.name("D:diffusion").value(diffusion);
		writer.name("I:heatCap").value(maxHeat);
	}
}
