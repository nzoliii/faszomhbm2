package com.hbm.fhbm2;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.hazard.HazardSystem;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.saveddata.RadiationSavedData;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class fhbm2KabanStatue extends BlockBase {

    private float radIn = 0.0F;
    private float radMax = 0.0F;
    private float rad3d = 0.0F;
    private ExtDisplayEffect extEffect = null;
    private boolean beaconable = false;

    public fhbm2KabanStatue(Material mat, String s) {
        super(mat, s);
    }

    public fhbm2KabanStatue(String s) {
        this(Material.IRON, s);
    }

    public fhbm2KabanStatue(Material mat, SoundType type, String s) {
        this(mat, s);
        setSoundType(type);
    }

    public fhbm2KabanStatue(SoundType type, String s) {
        this(Material.IRON, s);
        setSoundType(type);
    }

    public fhbm2KabanStatue setDisplayEffect(ExtDisplayEffect extEffect) {
        this.extEffect = extEffect;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);

        if (extEffect == null)
            return;

        switch (extEffect) {
            case RADFOG:
            case SCHRAB:
            case FLAMES:
                sPart(worldIn, pos.getX(), pos.getY(), pos.getZ(), rand);
                break;

            case LAVAPOP:
                worldIn.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
                break;

            default:
                break;
        }
    }

    private void sPart(World world, int x, int y, int z, Random rand) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (dir == ForgeDirection.DOWN && this.extEffect == ExtDisplayEffect.FLAMES)
                continue;

            if (world.getBlockState(new BlockPos(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)).getMaterial() == Material.AIR) {
                double ix = x + 0.5F + dir.offsetX + rand.nextDouble() * 3 - 1.5D;
                double iy = y + 0.5F + dir.offsetY + rand.nextDouble() * 3 - 1.5D;
                double iz = z + 0.5F + dir.offsetZ + rand.nextDouble() * 3 - 1.5D;

                if (dir.offsetX != 0)
                    ix = x + 0.5F + dir.offsetX * 0.5 + rand.nextDouble() * dir.offsetX;
                if (dir.offsetY != 0)
                    iy = y + 0.5F + dir.offsetY * 0.5 + rand.nextDouble() * dir.offsetY;
                if (dir.offsetZ != 0)
                    iz = z + 0.5F + dir.offsetZ * 0.5 + rand.nextDouble() * dir.offsetZ;

                if (this.extEffect == ExtDisplayEffect.RADFOG) {
                    world.spawnParticle(EnumParticleTypes.TOWN_AURA, ix, iy, iz, 0.0, 0.0, 0.0);
                }

                if (this.extEffect == ExtDisplayEffect.SCHRAB) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "schrabfog");
                    data.setDouble("posX", ix);
                    data.setDouble("posY", iy);
                    data.setDouble("posZ", iz);
                    MainRegistry.proxy.effectNT(data);
                }

                if (this.extEffect == ExtDisplayEffect.FLAMES) {
                    world.spawnParticle(EnumParticleTypes.FLAME, ix, iy, iz, 0.0, 0.0, 0.0);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.0, 0.0);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.1, 0.0);
                }
            }
        }
    }

    public fhbm2KabanStatue addRadiation(float radiation) {
        this.radIn = radiation * 0.1F;
        this.radMax = radiation;
        return this;
    }

    public fhbm2KabanStatue makeBeaconable() {
        this.beaconable = true;
        return this;
    }

    public fhbm2KabanStatue addRad3d(int rad3d) {
        this.rad3d = rad3d;
        return this;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return beaconable;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.rad3d > 0) {
            ContaminationUtil.radiate(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32, this.rad3d, 0, 0, 0, 0);
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }

        if (this == ModBlocks.block_meteor_molten) {
            if (!worldIn.isRemote)
                worldIn.setBlockState(pos, ModBlocks.block_meteor_cobble.getDefaultState());
            worldIn.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);
            return;
        }

        if (this.radIn > 0) {
            RadiationSavedData.incrementRad(worldIn, pos, radIn, radIn * 10F);
        }

        if (!worldIn.isRemote) {
            List<EntityPlayer> players = worldIn.getEntitiesWithinAABB(EntityPlayer.class,
                    new AxisAlignedBB(
                            pos.getX() - 30, pos.getY() - 30, pos.getZ() - 30,
                            pos.getX() + 30, pos.getY() + 30, pos.getZ() + 30
                    )
            );

            if (!players.isEmpty()) {
                for (EntityPlayer player : players) {
                    if (fhbm2KabanTracker.hasPlayersAteFragment(player)) {
                        // player.sendMessage(new TextComponentString("Fragment eaten, we do nothing."));
                    } else {
                        // player.sendMessage(new TextComponentString("Fragment not eaten yet, bewitching player."));

                        // copper_pig_bewitches_the_clueless_player(player, worldIn, pos.getX(), pos.getY(), pos.getZ());
                        copper_pig_bewitches_the_clueless_player_and_makes_them_experience_very_bad_schizophrenic_hallucinations(player, worldIn, pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            }
        }
    }

    @Override
    public int tickRate(World world) {
        if (this.rad3d > 0)
            return 20;
        if (this.radIn > 0)
            return 60 + world.rand.nextInt(500);
        return super.tickRate(world);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        if (this.radIn > 0 || this.rad3d > 0) {
            this.setTickRandomly(true);
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public void onPlayerDestroy(World world, BlockPos pos, IBlockState state) {
        if (this == ModBlocks.block_meteor_molten) {
            if (!world.isRemote)
                world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        }
    }

    public static enum ExtDisplayEffect {
        RADFOG, SPARKS, SCHRAB, FLAMES, LAVAPOP
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return;

        HazardSystem.applyHazards(this, (EntityLivingBase) entity);
        if (this == ModBlocks.brick_jungle_mystic) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.taint, 15 * 20, 2));
        }
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return;

        HazardSystem.applyHazards(this, (EntityLivingBase) entity);
        if (this == ModBlocks.brick_jungle_mystic) {
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.taint, 15 * 20, 2));
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.frozen_planks || this == ModBlocks.frozen_dirt) {
            return Items.SNOWBALL;
        }
        return Item.getItemFromBlock(this);
    }

    public void copper_pig_bewitches_the_clueless_player_and_makes_them_experience_very_bad_schizophrenic_hallucinations(EntityPlayer player, World world, int x, int y, int z) {
        if (fhbm2KabanTracker.isPlayerBewitched(player)) {
            return;
        }

        fhbm2Scheduler.schedule(0 * 20, (event) -> {
            fhbm2KabanTracker.setPlayerBewitched(player, true);
            world.playSound(null, x, y, z, HBMSoundHandler.fhbm2_copper_pig_bewitches_the_clueless_player, SoundCategory.HOSTILE, 35.0F, 1.0F);
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 600, 49));
        });

        fhbm2Scheduler.schedule(5 * 20, (event) -> {
            world.playSound(null, x, y, z, HBMSoundHandler.fhbm2_lobotomy, SoundCategory.HOSTILE, 35.0F, 1.0F);
            fhbm2CopperPigLobotomyCutscene.startPlayback(player.getUniqueID());
        });

        fhbm2Scheduler.schedule(25 * 20, (event) -> {
            world.playSound(null, x, y, z, HBMSoundHandler.fhbm2_copper_pig_explosion, SoundCategory.HOSTILE, 35.0F, 1.0F);
            player.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 1, 49));
            player.attackEntityFrom(ModDamageSource.copper_pig, 40);
            fhbm2KabanTracker.setPlayerBewitched(player, false);
        });
    }
}