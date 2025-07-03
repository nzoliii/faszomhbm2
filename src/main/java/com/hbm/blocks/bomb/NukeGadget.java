package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.fhbm2Scheduler;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.InventoryHelper;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityNukeGadget;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NukeGadget extends BlockContainer implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public NukeGadget(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityNukeGadget();
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		InventoryHelper.dropInventoryItems(worldIn, pos, worldIn.getTileEntity(pos));
		super.breakBlock(worldIn, pos, state);
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		} else if (!player.isSneaking()) {
			TileEntityNukeGadget entity = (TileEntityNukeGadget) world.getTileEntity(pos);
			if (entity != null) {
				player.openGui(MainRegistry.instance, ModBlocks.guiID_nuke_gadget, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		TileEntityNukeGadget entity = (TileEntityNukeGadget) worldIn.getTileEntity(pos);
		if (worldIn.getStrongPower(pos) > 0 && !worldIn.isRemote) {
			if (entity.isReady()) {
				this.onPlayerDestroy(worldIn, pos, state);
				entity.clearSlots();
				worldIn.setBlockToAir(pos);
				igniteTestBomb(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}
	}
	
	public boolean igniteTestBomb(World world, int x, int y, int z) {
		if (!world.isRemote) {

//			Back in the day when I was making this thing, I was trying to replicate the end portal opening method because every player hears it.
//			Well, it didn't work, or I was just clueless and I didn't know how to do it.
//			So I got this brilliant idea: just make the bomb play the sound, in a big range.

			world.playSound(null, x, y, z, HBMSoundHandler.fhbm2_oppenheimer, SoundCategory.PLAYERS, 50000.0F, 1.0F); // x,y,z,sound,volume,pitch

//			Used to freeze the game until Mr Oppenheimer finishes his speech.
//			Took me a while to actually fix this and not freeze the game.
//			Now I can just use my scheduler that was made for the bewitching.
//
//			try {
//				TimeUnit.MILLISECONDS.sleep(3500);
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
//
//			// world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, world.rand.nextFloat() * 0.1F + 0.9F); // x,y,z,sound,volume,pitch
//
//			world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.gadgetRadius, x + 0.5, y + 0.5, z + 0.5));
//			if (BombConfig.enableNukeClouds) {
//				EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.gadgetRadius);
//			}

			fhbm2Scheduler.schedule(70, (event) -> {
				world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.gadgetRadius, x + 0.5, y + 0.5, z + 0.5));
				if (BombConfig.enableNukeClouds) {
					EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.gadgetRadius);}
			});
		}

		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos) {
		if(!world.isRemote) {
			TileEntityNukeGadget entity = (TileEntityNukeGadget) world.getTileEntity(pos);
			// if (p_149695_1_.getStrongPower(x, y, z))
				if (entity.isReady()) {
					this.onPlayerDestroy(world, pos, world.getBlockState(pos));
					entity.clearSlots();
					world.setBlockToAir(pos);
					igniteTestBomb(world, pos.getX(), pos.getY(), pos.getZ());
					return BombReturnCode.DETONATED;
				}

			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}

		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}
	
	
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add("§2["+ I18nUtil.resolveKey("trait.nuclearbomb")+"]"+"§r");
		tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", BombConfig.gadgetRadius)+"§r");
		if(!BombConfig.disableNuclear){
			tooltip.add("§2["+ I18nUtil.resolveKey("trait.fallout")+"]"+"§r");
			tooltip.add(" §e"+I18nUtil.resolveKey("desc.radius", (int)BombConfig.gadgetRadius*(1+BombConfig.falloutRange/100))+"§r");
		}
	}
}
