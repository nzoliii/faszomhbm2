package com.hbm.tileentity;

import com.hbm.util.CompatExternal;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.ArrayList;

public interface IPersistentNBT {

    String NBT_PERSISTENT_KEY = "persistent";

    static ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, Block b) {

        TileEntity tile = CompatExternal.getCoreFromPos(world, pos);
        if (tile instanceof IPersistentNBT) {
            return ((IPersistentNBT) tile).getDrops(b);
        }

        return new ArrayList<>();
    }

    static void restoreData(World world, BlockPos pos, ItemStack stack) {
        try {
            if (!stack.hasTagCompound()) return;
            IPersistentNBT tile = (IPersistentNBT) world.getTileEntity(pos);
            tile.readNBT(stack.getTagCompound());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void writeNBT(NBTTagCompound nbt);

    void readNBT(NBTTagCompound nbt);

    default ArrayList<ItemStack> getDrops(Block b) {
        ArrayList<ItemStack> list = new ArrayList<>();
        ItemStack stack = new ItemStack(b);
        NBTTagCompound data = new NBTTagCompound();
        writeNBT(data);
        if (!data.isEmpty()) stack.setTagCompound(data);
        list.add(stack);
        return list;
    }
}
