package com.hbm.handler.imc;


import com.hbm.inventory.RecipesCommon;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;

import static net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCCentrifuge extends IMCHandler {

    public static HashMap<RecipesCommon.AStack, ItemStack[]> buffer = new HashMap();

    @Override
    public void process(IMCMessage message) {

        NBTTagCompound data = message.getNBTValue();
        ItemStack[] outs = new ItemStack[4];

        for (int i = 0; i < 4; i++) {

            NBTTagCompound output = data.getCompoundTag("output" + (i + 1));
            ItemStack out = new ItemStack(output);

            if (out == null) {
                this.printError(message, "Output stack could not be read!");
                return;
            }

            outs[i] = out;
        }

        NBTTagCompound input = data.getCompoundTag("input");
        ItemStack in = new ItemStack(input);

        if (in != null) {
            buffer.put(new RecipesCommon.ComparableStack(in), outs);
        } else {
            String dict = data.getString("oredict");

            if (!dict.isEmpty()) {
                buffer.put(new RecipesCommon.OreDictStack(dict), outs);
            } else {
                this.printError(message, "Input stack could not be read!");
            }
        }
    }
}
