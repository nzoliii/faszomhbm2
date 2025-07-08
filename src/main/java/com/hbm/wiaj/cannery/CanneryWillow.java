package com.hbm.wiaj.cannery;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.OreDictManager;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import com.hbm.wiaj.JarScene;
import com.hbm.wiaj.JarScript;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actions.*;
import com.hbm.wiaj.actors.ActorFancyPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_0;
import static com.hbm.blocks.PlantEnums.EnumFlowerPlantType.MUSTARD_WILLOW_1;
import static com.hbm.blocks.PlantEnums.EnumTallPlantType.*;

public class CanneryWillow extends CanneryBase {


    @Override
    public ItemStack getIcon() {
        return OreDictManager.DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.MUSTARDWILLOW);
    }

    @Override
    public String getName() {
        return "cannery.willow";
    }

    @Override
    public JarScript createScript() {

        WorldInAJar world = new WorldInAJar(5, 5, 5);
        JarScript script = new JarScript(world);

        JarScene scene0 = new JarScene(script);
        scene0.add(new ActionSetZoom(3, 0));

        for(int x = world.sizeX - 1; x >= 0 ; x--) {
            for(int z = 0; z < world.sizeZ; z++) {
                scene0.add(new ActionSetBlock(x, 1, z, Blocks.GRASS));
                scene0.add(new ActionSetBlock(x, 0, z, Blocks.DIRT));
            }
            scene0.add(new ActionWait(2));
        }

        scene0.add(new ActionWait(8));
        scene0.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_flower, MUSTARD_WILLOW_0.ordinal()));

        scene0.add(new ActionWait(10));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.0")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));

        scene0.add(new ActionWait(80));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.1")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));

        scene0.add(new ActionWait(80));
        scene0.add(new ActionRemoveActor(0));

        scene0.add(new ActionWait(10));
        scene0.add(new ActionSetBlock(2, 1, 1, Blocks.AIR));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionSetBlock(2, 1, 1, Blocks.WATER));

        scene0.add(new ActionWait(20));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.2")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));

        scene0.add(new ActionWait(80));
        scene0.add(new ActionRemoveActor(0));
        scene0.add(new ActionWait(20));
        scene0.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_flower, MUSTARD_WILLOW_0.ordinal()));

        scene0.add(new ActionWait(20));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.3")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));

        scene0.add(new ActionWait(80));
        scene0.add(new ActionRemoveActor(0));
        scene0.add(new ActionWait(20));
        scene0.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_2_LOWER.ordinal()));
        scene0.add(new ActionSetBlock(2, 3, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_2_UPPER.ordinal()));

        scene0.add(new ActionWait(20));
        scene0.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_3_LOWER.ordinal()));
        scene0.add(new ActionSetBlock(2, 3, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_3_UPPER.ordinal()));
        scene0.add(new ActionWait(20));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -35, new Object[][] {{I18nUtil.resolveKey("cannery.willow.4")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));
        scene0.add(new ActionWait(80));
        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -35, new Object[][] {{I18nUtil.resolveKey("cannery.willow.5")}}, 200)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));

        scene0.add(new ActionWait(100));
        scene0.add(new ActionRemoveActor(0));
        scene0.add(new ActionWait(20));
        scene0.add(new ActionSetBlock(2, 1, 2, ModBlocks.dirt_oily));

        scene0.add(new ActionWait(20));
        scene0.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_4_LOWER.ordinal()));
        scene0.add(new ActionSetBlock(2, 3, 2, ModBlocks.plant_tall, MUSTARD_WILLOW_4_UPPER.ordinal()));
        scene0.add(new ActionSetBlock(2, 1, 2, Blocks.DIRT));
        scene0.add(new ActionWait(20));

        scene0.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -35, new Object[][] {{I18nUtil.resolveKey("cannery.willow.6")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));
        scene0.add(new ActionWait(100));
        scene0.add(new ActionRemoveActor(0));

        JarScene scene1 = new JarScene(script);
        scene1.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -35, new Object[][] {{I18nUtil.resolveKey("cannery.willow.7")}}, 200)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));
        scene1.add(new ActionWait(100));
        scene1.add(new ActionRemoveActor(0));

        scene1.add(new ActionWait(20));
        scene1.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 10, -25, new Object[][] {{"=", OreDictManager.DictFrame.fromOne(ModBlocks.plant_flower, MUSTARD_WILLOW_1), "x1 + ", OreDictManager.DictFrame.fromOne(ModItems.plant_item, ItemEnums.EnumPlantType.MUSTARDWILLOW), "x3-6"}}, 0)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.LEFT)));
        scene1.add(new ActionSetBlock(2, 3, 2, Blocks.AIR));
        scene1.add(new ActionSetBlock(2, 2, 2, ModBlocks.plant_flower, MUSTARD_WILLOW_1.ordinal()));
        scene1.add(new ActionWait(60));

        scene1.add(new ActionRemoveActor(0));
        scene1.add(new ActionWait(20));
        scene1.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.8")}}, 200)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));
        scene1.add(new ActionWait(100));

        scene1.add(new ActionCreateActor(0, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -15, new Object[][] {{I18nUtil.resolveKey("cannery.willow.9")}}, 150)
                .setColors(colorCopper).setOrientation(ActorFancyPanel.Orientation.BOTTOM)));
        scene1.add(new ActionWait(100));
        scene1.add(new ActionRemoveActor(0));

        script.addScene(scene0).addScene(scene1);
        return script;
    }
}
