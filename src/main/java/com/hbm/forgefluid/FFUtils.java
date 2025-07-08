package com.hbm.forgefluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidTank;

@Deprecated
//Stays for now for compatibitly sake
public class FFUtils {


    public static NBTTagList serializeTankArray(FluidTank[] tanks) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < tanks.length; i++) {
            if (tanks[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("tank", (byte) i);
                tanks[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        return list;
    }

    public static void deserializeTankArray(NBTTagList tankList, FluidTank[] tanks) {
        for (int i = 0; i < tankList.tagCount(); i++) {
            NBTTagCompound tag = tankList.getCompoundTagAt(i);
            byte b0 = tag.getByte("tank");
            if (b0 >= 0 && b0 < tanks.length) {
                tanks[b0].readFromNBT(tag);
            }
        }
    }

}
