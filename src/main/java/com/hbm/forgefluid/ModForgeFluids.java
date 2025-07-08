package com.hbm.forgefluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.fluid.*;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.HashMap;

@Mod.EventBusSubscriber(modid = RefStrings.MODID)
@Deprecated
public class ModForgeFluids {

	public static HashMap<Fluid, Integer> fluidColors = new HashMap<Fluid, Integer>();


	//Block fluids
	public static Fluid toxic_fluid = new ToxicFluid("toxic_fluid").setDensity(2500).setViscosity(2000).setTemperature(70+273);
	public static Fluid radwater_fluid = new RadWaterFluid("radwater_fluid").setDensity(1000);
	public static Fluid mud_fluid = new MudFluid().setDensity(2500).setViscosity(3000).setLuminosity(5).setTemperature(1773);
	public static Fluid schrabidic = new SchrabidicFluid("schrabidic").setDensity(31200).setViscosity(500);
	public static Fluid corium_fluid = new CoriumFluid().setDensity(31200).setViscosity(2000).setTemperature(3000);
	public static Fluid volcanic_lava_fluid = new VolcanicFluid().setLuminosity(15).setDensity(3000).setViscosity(3000).setTemperature(1300);
	public static Fluid bromine_fluid = new Fluid("bromine_fluid", new ResourceLocation(RefStrings.MODID, "blocks/bromine_still"), new ResourceLocation(RefStrings.MODID, "blocks/bromine_flowing"), null, Color.WHITE).setDensity(3000).setViscosity(3000).setTemperature(273);
	
	public static void init() {
		if(!FluidRegistry.registerFluid(toxic_fluid))
			toxic_fluid = FluidRegistry.getFluid("toxic_fluid");
		if(!FluidRegistry.registerFluid(radwater_fluid))
			radwater_fluid = FluidRegistry.getFluid("radwater_fluid");
		if(!FluidRegistry.registerFluid(mud_fluid))
			mud_fluid = FluidRegistry.getFluid("mud_fluid");
		if(!FluidRegistry.registerFluid(schrabidic))
			schrabidic = FluidRegistry.getFluid("schrabidic");
		if(!FluidRegistry.registerFluid(corium_fluid))
			corium_fluid = FluidRegistry.getFluid("corium_fluid");
		if(!FluidRegistry.registerFluid(volcanic_lava_fluid))
			volcanic_lava_fluid = FluidRegistry.getFluid("volcanic_lava_fluid");
		if(!FluidRegistry.registerFluid(bromine_fluid))
			bromine_fluid = FluidRegistry.getFluid("bromine_fluid");

		ModBlocks.toxic_block = new ToxicBlock(ModForgeFluids.toxic_fluid, ModBlocks.fluidtoxic, ModDamageSource.radiation, "toxic_block").setResistance(500F);
		ModBlocks.radwater_block = new RadWaterBlock(ModForgeFluids.radwater_fluid, ModBlocks.fluidradwater, ModDamageSource.radiation, "radwater_block").setResistance(500F);
		ModBlocks.mud_block = new MudBlock(ModForgeFluids.mud_fluid, ModBlocks.fluidmud, ModDamageSource.mudPoisoning, "mud_block").setResistance(500F);
		ModBlocks.schrabidic_block = new SchrabidicBlock(schrabidic, ModBlocks.fluidschrabidic.setReplaceable(), ModDamageSource.radiation, "schrabidic_block").setResistance(500F);
		ModBlocks.corium_block = new CoriumBlock(corium_fluid, ModBlocks.fluidcorium, "corium_block").setResistance(500F);
		ModBlocks.volcanic_lava_block = new VolcanicBlock(volcanic_lava_fluid, ModBlocks.fluidvolcanic, "volcanic_lava_block").setResistance(500F);
		ModBlocks.bromine_block = new BlockFluidClassic(bromine_fluid, Material.WATER).setResistance(500F);
		toxic_fluid.setBlock(ModBlocks.toxic_block);
		radwater_fluid.setBlock(ModBlocks.radwater_block);
		mud_fluid.setBlock(ModBlocks.mud_block);
		schrabidic.setBlock(ModBlocks.schrabidic_block);
		corium_fluid.setBlock(ModBlocks.corium_block);
		volcanic_lava_fluid.setBlock(ModBlocks.volcanic_lava_block);
		bromine_fluid.setBlock(ModBlocks.bromine_block);
		FluidRegistry.addBucketForFluid(toxic_fluid);
		FluidRegistry.addBucketForFluid(radwater_fluid);
		FluidRegistry.addBucketForFluid(mud_fluid);
		FluidRegistry.addBucketForFluid(schrabidic);
		FluidRegistry.addBucketForFluid(corium_fluid);
		FluidRegistry.addBucketForFluid(volcanic_lava_fluid);
	}

	//Stupid forge reads a bunch of default fluids from NBT when the world loads, which screws up my logic for replacing my fluids with fluids from other mods.
	//Forge does this in a place with apparently no events surrounding it. It calls a method in the mod container, but I've
	//been searching for an hour now and I have found no way to make your own custom mod container.
	//Would it have killed them to add a simple event there?!?
	public static void setFromRegistry() {
		toxic_fluid = FluidRegistry.getFluid("toxic_fluid");
		radwater_fluid = FluidRegistry.getFluid("radwater_fluid");
		mud_fluid = FluidRegistry.getFluid("mud_fluid");
		schrabidic = FluidRegistry.getFluid("schrabidic");
		corium_fluid = FluidRegistry.getFluid("corium_fluid");
	}

	@SubscribeEvent
	public static void worldLoad(WorldEvent.Load evt) {
		setFromRegistry();
	}

	public static void registerFluidColors(){
		for(Fluid f : FluidRegistry.getRegisteredFluids().values()){
			fluidColors.put(f, Library.getColorFromResourceLocation(new ResourceLocation(f.getStill().getNamespace(), "textures/"+f.getStill().getPath()+".png")));
		}
	}

	public static int getFluidColor(Fluid f){
		if(f == null)
			return 0;
		Integer color = fluidColors.get(f);
		if(color == null)
			return 0xFFFFFF;
		return color;
	}
}
