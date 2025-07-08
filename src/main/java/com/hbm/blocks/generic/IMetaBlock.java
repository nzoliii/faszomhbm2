package com.hbm.blocks.generic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

/**
 * Interface for blocks with metadata.
 * This interface defines methods that HAS TO be implemented by blocks that use metadata.
 */
public interface IMetaBlock {

    /**
     * Gets the metadata value from the block state.
     * @param state The block state.
     * @return The metadata value.
     */
    int getMetaFromState(IBlockState state);

    /**
     * Gets the block state from the metadata value.
     * @param meta The metadata value.
     * @return The block state.
     */
    IBlockState getStateFromMeta(int meta);

    /**
     * Gets the drops for the block.
     * @param world The world.
     * @param pos The block position.
     * @param state The block state.
     * @param fortune The fortune level.
     * @return The drops.
     */
    List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune);

    /**
     * Gets the item stack when the block is picked.
     * NOTE: Used in The One Probe.
     * @param state The block state.
     * @param target The ray trace result.
     * @param world The world.
     * @param pos The block position.
     * @param player The player.
     * @return The item stack.
     */
    ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player);

    /**
     * Called when the block is placed.
     * @param world The world.
     * @param pos The block position.
     * @param state The block state.
     * @param placer The entity that placed the block.
     * @param stack The item stack.
     */
    void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack);
}