package com.hbm.inventory;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

//TODO: clean this shit up
//Alcater: on it
//Alcater: almost done yay
public class MachineRecipes {

    //return: FluidType, amount produced, amount required, heat required (°C * 100)
    public static Object[] getBoilerOutput(FluidType type) {

        if (type == Fluids.OIL) return new Object[]{Fluids.HOTOIL, 5, 5, 35000};
        if (type == Fluids.CRACKOIL) return new Object[]{Fluids.HOTCRACKOIL, 5, 5, 35000};

        return null;
    }
}
