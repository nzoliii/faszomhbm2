package com.hbm.blocks.generic;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.BedrockOreRegistry;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class BlockBedrockOreTE extends BlockContainer implements ILookOverlay {

	public BlockBedrockOreTE(String s) {
		super(Material.ROCK);
		this.setTranslationKey(s);
		this.setRegistryName(s);

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBedrockOre("oreIron");
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		entityIn.setFire(3);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));

		if(!(te instanceof TileEntityBedrockOre))
			return;

		TileEntityBedrockOre ore = (TileEntityBedrockOre) te;

		List<String> text = new ArrayList<>();
		text.add(I18nUtil.resolveKey("desc.ore", BedrockOreRegistry.getOreName(ore.oreName)));
		text.add(I18nUtil.resolveKey("desc.tier", ore.tier));

		if(ore.acidRequirement != null) {
			text.add(I18nUtil.resolveKey("desc.requires", ore.acidRequirement.fill, ore.acidRequirement.type.getLocalizedName()));
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	public static class TileEntityBedrockOre extends TileEntity {

		public String oreName;
		public int color;
		public int tier;
		public FluidStack acidRequirement;

		public TileEntityBedrockOre() {
		}

		public TileEntityBedrockOre(String oreName) {
			this.oreName = oreName;
			this.color = BedrockOreRegistry.getOreColor(oreName);
			this.tier = BedrockOreRegistry.getOreTier(oreName);
			this.acidRequirement = BedrockOreRegistry.getFluidRequirement(this.tier);
		}

		public TileEntityBedrockOre setOre(String oreName){
			this.oreName = oreName;
			this.color = BedrockOreRegistry.getOreColor(oreName);
			this.tier = BedrockOreRegistry.getOreTier(oreName);
			this.acidRequirement = BedrockOreRegistry.getFluidRequirement(this.tier);
			this.markDirty();
			return this;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.oreName = nbt.getString("ore");
			this.tier = nbt.getInteger("tier");
			this.color = nbt.getInteger("color");
			FluidType type = Fluids.fromID(nbt.getInteger("fluid"));

			if(type != Fluids.NONE) {
				this.acidRequirement = new FluidStack(type, nbt.getInteger("amount"));
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setString("ore", this.oreName);
			nbt.setInteger("tier", this.tier);
			nbt.setInteger("color", this.color);
			if(this.acidRequirement != null) {
				nbt.setInteger("fluid", this.acidRequirement.type.getID());
				nbt.setInteger("amount", this.acidRequirement.fill);
			}
			return nbt;
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket(){
			return new SPacketUpdateTileEntity(this.getPos(), 0, this.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public NBTTagCompound getUpdateTag() {
			return this.writeToNBT(new NBTTagCompound());
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}
	}
}
