package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class WasteEarth extends Block {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

    public WasteEarth(Material materialIn, boolean tick, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        this.setTickRandomly(tick);
        this.setHarvestLevel("shovel", 0);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    public WasteEarth(Material materialIn, SoundType type, boolean tick, String s) {
        this(materialIn, tick, s);
        setSoundType(type);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, meta);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.frozen_grass) {
            return Items.SNOWBALL;
        }
        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 1;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase castedEntity = ((EntityLivingBase) entity);

            if (this == ModBlocks.waste_earth) {
                castedEntity.addPotionEffect(new PotionEffect(HbmPotion.radiation, 15 * 20, 4));
            } else if (this == ModBlocks.waste_dirt) {
                castedEntity.addPotionEffect(new PotionEffect(HbmPotion.radiation, 20 * 20, 9));
            } else if (this == ModBlocks.waste_mycelium) {
                castedEntity.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 29));
                castedEntity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 5 * 20, 0));
            } else if (this == ModBlocks.frozen_grass) {
                castedEntity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2 * 60 * 20, 2));
            } else if (this == ModBlocks.burning_earth) {
                entity.setFire(3);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);

        if (this == ModBlocks.waste_earth || this == ModBlocks.waste_mycelium) {
            worldIn.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return ContaminationUtil.isRadImmune(entityIn);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.waste_earth || this == ModBlocks.waste_dirt || this == ModBlocks.waste_mycelium) {
            if (GeneralConfig.enableAutoCleanup) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
        }
    }
}
