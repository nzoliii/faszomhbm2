package com.hbm.blocks.machine.pile;

import com.hbm.api.block.IToolable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.special.ItemCell;
import net.minecraft.item.ItemStack;

public class BlockGraphiteBreedingProduct extends BlockGraphiteDrilledBase implements IToolable {

    public BlockGraphiteBreedingProduct(String s) {
        super(s);
    }

    @Override
    protected ItemStack getInsertedItem() {
        return ItemCell.getFullCell(Fluids.TRITIUM);
    }
}
