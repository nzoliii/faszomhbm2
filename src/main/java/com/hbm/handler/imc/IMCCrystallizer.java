package com.hbm.handler.imc;


import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.fluid.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.HashMap;

public class IMCCrystallizer extends IMCHandler {

    public static HashMap<Tuple.Pair<Object, FluidType>, CrystallizerRecipes.CrystallizerRecipe> buffer = new HashMap();

    @Override
    public void process(FMLInterModComms.IMCMessage message) {

        NBTTagCompound data = message.getNBTValue();

        NBTTagCompound output = data.getCompoundTag("output");
        ItemStack out = new ItemStack(output);

        if(out == null) {
            this.printError(message, "Output stack could not be read!");
            return;
        }

        NBTTagCompound input = data.getCompoundTag("input");
        ItemStack in = new ItemStack(input);

        int time = data.getInteger("duration");
        FluidStack acid = new FluidStack(Fluids.fromID(data.getInteger("acid")), data.getInteger("amount"));

        if(time <= 0)
            time = 600;

        if(acid.type == Fluids.NONE)
            acid = new FluidStack(Fluids.PEROXIDE, 500);

        CrystallizerRecipes.CrystallizerRecipe recipe = new CrystallizerRecipes.CrystallizerRecipe(out, time);
        recipe.acidAmount = acid.fill;

        if(in != null) {
            buffer.put(new Tuple.Pair(new RecipesCommon.ComparableStack(in), acid.type), recipe);
        } else {
            String dict = data.getString("oredict");

            if(!dict.isEmpty()) {
                buffer.put(new Tuple.Pair(dict, acid.type), recipe);
            } else {
                this.printError(message, "Input stack could not be read!");
            }
        }
    }
}
