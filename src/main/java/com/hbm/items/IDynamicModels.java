package com.hbm.items;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Used in items that require model baking;
 * Will automatically bake once correct methods are supplied
 */
public interface IDynamicModels {

    /**
     * Should be populated by implementors in constructors.
     */
    List<IDynamicModels> INSTANCES = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    static void bakeModels(ModelBakeEvent event) {
        INSTANCES.forEach(blockMeta -> blockMeta.bakeModel(event));
    }

    @SideOnly(Side.CLIENT)
    static void registerModels() {
        INSTANCES.forEach(IDynamicModels::registerModel);
    }

    @SideOnly(Side.CLIENT)
    static void registerSprites(TextureMap map) {
        INSTANCES.forEach(dynamicSprite -> dynamicSprite.registerSprite(map));
    }

    public static void registerCustomStateMappers() {
        for (IDynamicModels model : INSTANCES) {
            if (model.getBlock() == null) continue;
            StateMapperBase mapper = model.getStateMapper(model.getBlock().getRegistryName());
            if (mapper != null)
                ModelLoader.setCustomStateMapper(
                        model.getBlock(),
                        mapper
                );
        }

    }

    void bakeModel(ModelBakeEvent event);

    Block getBlock();

    void registerModel();

    @SideOnly(Side.CLIENT)
    void registerSprite(TextureMap map);

    @SideOnly(Side.CLIENT)
    default StateMapperBase getStateMapper(ResourceLocation loc) {
        return null;
    }
}
