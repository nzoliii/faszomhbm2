package com.hbm.fhbm2;

import com.hbm.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class fhbm2CreativeTab extends CreativeTabs {

    public fhbm2CreativeTab(int index, String label) {
        super(index, label);
    }

    @Override
    public ItemStack createIcon() {
        if(ModBlocks.fhbm2_kaban_statue != null){
            return new ItemStack(ModBlocks.fhbm2_kaban_statue);
        }
        return new ItemStack(Items.IRON_PICKAXE);
    }
}
