package com.hbm.interfaces;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import java.util.Set;

public interface IHasCustomMetaModels {

	public Set<Integer> getMetaValues();

	public ModelResourceLocation getResourceLocation(int meta);
}
