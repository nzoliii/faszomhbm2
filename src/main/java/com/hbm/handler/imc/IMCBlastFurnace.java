package com.hbm.handler.imc;

import com.hbm.inventory.RecipesCommon;
import com.hbm.util.Tuple;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.ArrayList;

public class IMCBlastFurnace extends IMCHandler {

    public static final ArrayList<Tuple.Triplet<Object, Object, ItemStack>> buffer = new ArrayList<>();

    @Override
    public void process(FMLInterModComms.IMCMessage message) {

        final NBTTagCompound data = message.getNBTValue();
        final NBTTagCompound outputData = data.getCompoundTag("output");
        final ItemStack output = new ItemStack(outputData);

        if(output == null) {
            printError(message, "Output stack could not be read!");
            return;
        }

        final Object input1;
        final Object input2;

        switch(data.getString("inputType1")) {
            case "ore":
                input1 = data.getString("input1");
                break;

            case "orelist":
                final NBTTagList list = data.getTagList("input1", 8);
                final ArrayList<String> ores = new ArrayList<String>(list.tagCount());
                for(int i = 0; i < list.tagCount(); i++)
                    ores.add(list.getStringTagAt(i));
                input1 = ores;
                break;

            case "itemstack":
                input1 = new RecipesCommon.ComparableStack(new ItemStack(data.getCompoundTag("input1")));
                break;

            default:
                printError(message, "Unhandled input type!");
                return;
        }

        switch(data.getString("inputType2")) {
            case "ore":
                input2 = data.getString("input2");
                break;

            case "orelist":
                final NBTTagList list = data.getTagList("input2", 9);
                final ArrayList<String> ores = new ArrayList<String>(list.tagCount());
                for(int i = 0; i < list.tagCount(); i++)
                    ores.add(list.getStringTagAt(i));
                input2 = ores;
                break;

            case "itemstack":
                input2 = new RecipesCommon.ComparableStack(new ItemStack(data.getCompoundTag("input2")));
                break;

            default:
                printError(message, "Unhandled input type!");
                return;
        }

        buffer.add(new Tuple.Triplet<Object, Object, ItemStack>(input1, input2, output));
    }
}
