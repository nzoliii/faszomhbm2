package com.hbm.render.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class RotatableStateMapper extends StateMapperBase {
    private final String registryName;

    public RotatableStateMapper(ResourceLocation blockName) {
        this.registryName = blockName.toString();
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        EnumFacing facing = state.getValue(BlockHorizontal.FACING);
        return new ModelResourceLocation(registryName, "facing=" + facing.getName());
    }
}
