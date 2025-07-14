package com.hbm.handler.neutron;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public abstract class NeutronNode {

    protected NeutronStream.NeutronType type;

    protected BlockPos pos;

    protected TileEntity tile;

    // like NBT but less fucking CANCER
    // Holds things like cached RBMK lid values.
    protected Map<String, Object> data = new HashMap<>();

    public NeutronNode(TileEntity tile, NeutronStream.NeutronType type) {
        this.type = type;
        this.tile = tile;
        this.pos = tile.getPos();
    }
}
