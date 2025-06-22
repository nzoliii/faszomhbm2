package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

public class ItemZirnoxRodDepleted extends ItemEnumMulti {

    public ItemZirnoxRodDepleted(String registryName) {
        super(registryName, EnumZirnoxTypeDepleted.class, true, true);
        this.canRepair = false;
    }

    public enum EnumZirnoxTypeDepleted {
        NATURAL_URANIUM_FUEL,
        URANIUM_FUEL,
        THORIUM_FUEL,
        MOX_FUEL,
        PLUTONIUM_FUEL,
        U233_FUEL,
        U235_FUEL,
        LES_FUEL,
        ZFB_MOX_FUEL,
    }
}
