package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.inventory.CentrifugeRecipes;
import com.hbm.inventory.DFCRecipes;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.ShredderRecipes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.*;
import com.hbm.items.EffectItem;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.main.MainRegistry;
import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@JEIPlugin
public class JEIConfig implements IModPlugin {

    public static final String ASSEMBLY = "hbm.assembly";
    public static final String CHEMPLANT = "hbm.chemplant";
    public static final String MIXER = "hbm.mixer";
    public static final String CYCLOTRON = "hbm.cyclotron";
    public static final String PRESS = "hbm.press";
    public static final String ALLOY = "hbm.alloy";
    public static final String BOILER = "hbm.boiler";
    public static final String BREEDER = "hbm.breeder";
    public static final String CENTRIFUGE = "hbm.centrifuge";
    public static final String CMB = "hbm.cmb_furnace";
    public static final String COKER = "hbm.coker";
    public static final String GAS_CENT = "hbm.gas_centrifuge";
    public static final String REFINERY = "hbm.refinery";
    public static final String CRACKING = "hbm.cracking";
    public static final String FRACTIONING = "hbm.fracturing";
    public static final String SHREDDER = "hbm.shredder";
    public static final String FLUIDS = "hbm.fluids";
    public static final String CRYSTALLIZER = "hbm.crystallizer";
    public static final String BOOK = "hbm.book_of";
    public static final String FUSION_BYPRODUCT = "hbm.fusionbyproduct";
    public static final String HADRON = "hbm.hadron";
    public static final String HYDROTREATING = "hbm.hydrotreating";
    public static final String LIQUEFACTION = "hbm.liquefaction";
    public static final String REFORMING = "hbm.reforming";
    public static final String SOLIDIFICATION = "hbm.solidification";
    public static final String SILEX = "hbm.silex";
    public static final String SILEX_RADIO = "hbm.silexradio";
    public static final String SILEX_MICRO = "hbm.silexmicro";
    public static final String SILEX_IR = "hbm.silexir";
    public static final String SILEX_VISIBLE = "hbm.silexvisible";
    public static final String SILEX_UV = "hbm.silexuv";
    public static final String SILEX_XRAY = "hbm.silexray";
    public static final String SILEX_GAMMA = "hbm.silexgamma";
    public static final String SILEX_DIGAMMA = "hbm.silexdigamma";
    public static final String WASTEDRUM = "hbm.waste_drum";
    public static final String STORAGEDRUM = "hbm.storage_drum";
    public static final String SMITHING = "hbm.smithing";
    public static final String ANVIL = "hbm.anvil";
    public static final String RBMKOUTGASSER = "hbm.rbmk_outgasser";
    public static final String RBMKFUEL = "hbm.rbmkfueluncrafting";
    public static final String DFC = "hbm.dfc";
    public static final String TRANSMUTATION = "hbm.transmutation";
    public static final String SOLDERING_STATION = "hbm.soldering_station";
    public static final String ARC_WELDER = "hbm.arc_welder";
    public static final String ROTARY_FURNACE = "hbm.rotary_furnace";
    private CokingRecipeHandler cokingHandler;
    private CrackingHandler crackingHandler;
    private CrystallizerRecipeHandler crystallizerHandler;
    private FractioningRecipeHandler fractioningHandler;
    private HydrotreatingHandler hydrotreatHandler;
    private LiquefactionHandler liquefactHandler;
    private MixerRecipeHandler mixerHandler;
    private RBMKOutgasserRecipeHandler outgasserHandler;
    private ReformingHandler reformingHandler;
    private SolidificationHandler solidificationHandler;
    private SolderingStationRecipeHandler solderingStationHandler;
    private ArcWelderRecipeHandler arcWelderRecipeHandler;
    private RotaryFurnaceRecipeHandler rotaryFurnaceRecipeHandler;

    @Override
    public void register(@NotNull IModRegistry registry) {
        if (!GeneralConfig.jei)
            return;
        registry.addRecipeRegistryPlugin(new HbmJeiRegistryPlugin());

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_electric_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.furnace_iron), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.furnace_steel), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_arc_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.factory_titanium_furnace), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.factory_advanced_furnace), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_microwave), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_nuke_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_rtg_furnace_off), VanillaRecipeCategoryUid.SMELTING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.crate_tungsten), VanillaRecipeCategoryUid.SMELTING);

        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_coker), COKER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_assembler), ASSEMBLY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_assemfac), ASSEMBLY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_chemplant), CHEMPLANT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_chemfac), CHEMPLANT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_cyclotron), CYCLOTRON);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_schrabidium_transmutator), TRANSMUTATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_press), PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_epress), PRESS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_difurnace_off), ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_difurnace_rtg_off), ALLOY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_solar_boiler), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.heat_boiler), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.rbmk_heater), BOILER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_reactor_breeding), BREEDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_centrifuge), CENTRIFUGE);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_gascent), GAS_CENT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_waste_drum), WASTEDRUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_storage_drum), STORAGEDRUM);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_refinery), REFINERY);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_catalytic_cracker), CRACKING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_fraction_tower), FRACTIONING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_hydrotreater), HYDROTREATING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_liquefactor), LIQUEFACTION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_catalytic_reformer), REFORMING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_solidifier), SOLIDIFICATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_mixer), MIXER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_shredder), SHREDDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_fluidtank), FLUIDS);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_crystallizer), CRYSTALLIZER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_soldering_station), SOLDERING_STATION);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_arc_welder), ARC_WELDER);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_rotary_furnace), ROTARY_FURNACE);
        //This recipe catalyst doesn't work, since the book of is blacklisted.
        registry.addRecipeCatalyst(new ItemStack(ModItems.book_of_), BOOK);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.iter), FUSION_BYPRODUCT);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.hadron_core), HADRON);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.machine_silex), SILEX);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_nano), SILEX_RADIO);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_pentacene), SILEX_MICRO);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_co2), SILEX_IR);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_bismuth), SILEX_VISIBLE);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_cmb), SILEX_UV);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_dem), SILEX_XRAY);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_bale), SILEX_GAMMA);
        registry.addRecipeCatalyst(new ItemStack(ModItems.laser_crystal_digamma), SILEX_DIGAMMA);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_iron), SMITHING);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.anvil_steel), ANVIL);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.rbmk_outgasser), RBMKOUTGASSER);
        registry.addRecipeCatalyst(new ItemStack(Objects.requireNonNull(Blocks.CRAFTING_TABLE)), RBMKFUEL);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.crate_tungsten), DFC);

        // registry.addRecipes(ItemAssemblyTemplate.recipes, ASSEMBLY);
        registry.addRecipes(JeiRecipes.getChemistryRecipes(), CHEMPLANT);
        registry.addRecipes(JeiRecipes.getCyclotronRecipes(), CYCLOTRON);
        registry.addRecipes(JeiRecipes.getTransmutationRecipes(), TRANSMUTATION);
        registry.addRecipes(JeiRecipes.getPressRecipes(), PRESS);
        registry.addRecipes(JeiRecipes.getAlloyRecipes(), ALLOY);
        registry.addRecipes(CentrifugeRecipes.getCentrifugeRecipes(), CENTRIFUGE);
        registry.addRecipes(JeiRecipes.getCMBRecipes(), CMB);
        registry.addRecipes(JeiRecipes.getGasCentrifugeRecipes(), GAS_CENT);
        registry.addRecipes(JeiRecipes.getWasteDrumRecipes(), WASTEDRUM);
        registry.addRecipes(JeiRecipes.getStorageDrumRecipes(), STORAGEDRUM);
        registry.addRecipes(JeiRecipes.getRefineryRecipe(), REFINERY);
        registry.addRecipes(crackingHandler.getRecipes(), CRACKING);
        registry.addRecipes(fractioningHandler.getRecipes(), FRACTIONING);
        registry.addRecipes(hydrotreatHandler.getRecipes(), HYDROTREATING);
        registry.addRecipes(liquefactHandler.getRecipes(), LIQUEFACTION);
        registry.addRecipes(mixerHandler.getRecipes(), MIXER);
        registry.addRecipes(outgasserHandler.getRecipes(), RBMKOUTGASSER);
        registry.addRecipes(reformingHandler.getRecipes(), REFORMING);
        registry.addRecipes(solidificationHandler.getRecipes(), SOLIDIFICATION);
        registry.addRecipes(solderingStationHandler.getRecipes(), SOLDERING_STATION);
        registry.addRecipes(arcWelderRecipeHandler.getRecipes(), ARC_WELDER);
        registry.addRecipes(rotaryFurnaceRecipeHandler.getRecipes(), ROTARY_FURNACE);
        registry.addRecipes(ShredderRecipes.getShredderRecipes(), SHREDDER);
        registry.addRecipes(JeiRecipes.getFluidEquivalences(), FLUIDS);
        registry.addRecipes(crystallizerHandler.getRecipes(), CRYSTALLIZER);
        registry.addRecipes(JeiRecipes.getBookRecipes(), BOOK);
        registry.addRecipes(JeiRecipes.getBreederRecipes(), BREEDER);
        registry.addRecipes(cokingHandler.getRecipes(), COKER);
        registry.addRecipes(JeiRecipes.getFusionByproducts(), FUSION_BYPRODUCT);
        registry.addRecipes(JeiRecipes.getHadronRecipes(), HADRON);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(), SILEX);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.RADIO), SILEX_RADIO);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.MICRO), SILEX_MICRO);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.IR), SILEX_IR);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.VISIBLE), SILEX_VISIBLE);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.UV), SILEX_UV);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.XRAY), SILEX_XRAY);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.GAMMA), SILEX_GAMMA);
        registry.addRecipes(JeiRecipes.getSILEXRecipes(EnumWavelengths.DRX), SILEX_DIGAMMA);
        registry.addRecipes(JeiRecipes.getSmithingRecipes(), SMITHING);
        registry.addRecipes(JeiRecipes.getAnvilRecipes(), ANVIL);
        registry.addRecipes(JeiRecipes.getRBMKFuelRecipes(), RBMKFUEL);
        registry.addRecipes(DFCRecipes.getDFCRecipes(), DFC);


        registry.addRecipeClickArea(GUIMachineCoker.class, 60, 22, 32, 18, COKER);
        registry.addRecipeClickArea(GUIMachineAssembler.class, 45, 83, 82, 30, ASSEMBLY);
		registry.addRecipeClickArea(GUIMachineChemplant.class, 45, 90, 85, 15, CHEMPLANT);
		registry.addRecipeClickArea(GUIMixer.class, 62, 36, 52, 44, MIXER);
		registry.addRecipeClickArea(GUIMachineCyclotron.class, 50, 24, 40, 40, CYCLOTRON);
		registry.addRecipeClickArea(GUIMachinePress.class, 80, 35, 15, 15, PRESS);
		registry.addRecipeClickArea(GUIMachineEPress.class, 80, 35, 15, 15, PRESS);
		registry.addRecipeClickArea(GUIDiFurnace.class, 102, 36, 21, 14, ALLOY);
		registry.addRecipeClickArea(GUIDiFurnaceRTG.class, 102, 36, 21, 14, ALLOY);
		registry.addRecipeClickArea(GUIMachineCentrifuge.class, 35, 9, 106, 40, CENTRIFUGE);
		registry.addRecipeClickArea(GUIMachineGasCent.class, 70, 36, 36, 12, GAS_CENT);
        registry.addRecipeClickArea(GUIMachineSolderingStation.class, 72, 29, 32, 13, SOLDERING_STATION);
		registry.addRecipeClickArea(GUIMachineReactorBreeding.class, 73, 32, 30, 20, BREEDER);
		registry.addRecipeClickArea(GUIMachineRefinery.class, 53, 30, 56, 85, REFINERY);
		registry.addRecipeClickArea(GUIMachineShredder.class, 43, 89, 53, 17, SHREDDER);
		registry.addRecipeClickArea(GUICrystallizer.class, 79, 40, 29, 26, CRYSTALLIZER);
		registry.addRecipeClickArea(GUIBook.class, 89, 34, 23, 16, BOOK);
		registry.addRecipeClickArea(GUIHadron.class, 71, 28, 32, 32, HADRON);
		registry.addRecipeClickArea(GUISILEX.class, 45, 82, 113-45, 125-82, SILEX);
		registry.addRecipeClickArea(GUIAnvil.class, 34, 26, 52-34, 44-26, SMITHING);
		registry.addRecipeClickArea(GUIAnvil.class, 12, 50, 48-12, 66-50, ANVIL);
		registry.addRecipeClickArea(GUIRBMKOutgasser.class, 64, 53, 48, 16, RBMKOUTGASSER);
		registry.addRecipeClickArea(GUIMachineSchrabidiumTransmutator.class, 64, 56, 66, 31, TRANSMUTATION);
        registry.addRecipeClickArea(GUIMachineArcWelder.class, 72, 38, 32, 13, ARC_WELDER);
        registry.addRecipeClickArea(GUIMachineRotaryFurnace.class, 63, 31, 32, 9, ROTARY_FURNACE);

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();

        // Some things are even beyond my control...or are they?
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.memory));

        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_electric_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_arc_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_difurnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_nuke_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.machine_rtg_furnace_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.reinforced_lamp_on));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_g));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_w));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.statue_elb_f));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.cheater_virus));
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.cheater_virus_seed));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_helmet));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_plate));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_legs));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_boots));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.apple_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ingot_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.nugget_euphemium));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_kit));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.euphemium_stopper));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.watch));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.rod_quad_euphemium));
        // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.rod_euphemium));
        blacklist.addIngredientToBlacklist(new ItemStack(ModItems.bobmazon_hidden));
        if (MainRegistry.polaroidID != 11) {
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.book_secret));
            // blacklist.addIngredientToBlacklist(new ItemStack(ModItems.book_of_));
            blacklist.addIngredientToBlacklist(new ItemStack(ModItems.ams_core_thingy));
        }
        //Th3_Sl1ze: gladly we DON'T have old dummy blocks now.. and un-fucking-gladly..
        //TODO: fix the newly-old ported dummyfied machines
        blacklist.addIngredientToBlacklist(new ItemStack(ModBlocks.dummy_block_silo_hatch));


        for (Item item : ModItems.ALL_ITEMS) {
            if (item instanceof EffectItem) {
                blacklist.addIngredientToBlacklist(new ItemStack(item));
            }
        }
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registry) {
        if (!GeneralConfig.jei)
            return;
        IGuiHelper help = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new AnvilRecipeHandler(help),
                new SmithingRecipeHandler(help),
                new PressRecipeHandler(help),
                new AlloyFurnaceRecipeHandler(help),
                new ShredderRecipeHandler(help),
                new AssemblerRecipeHandler(help),
                new ChemplantRecipeHandler(help),
                new RefineryRecipeHandler(help),
                cokingHandler = new CokingRecipeHandler(help),
                crackingHandler = new CrackingHandler(help),
                crystallizerHandler = new CrystallizerRecipeHandler(help),
                fractioningHandler = new FractioningRecipeHandler(help),
                hydrotreatHandler = new HydrotreatingHandler(help),
                liquefactHandler = new LiquefactionHandler(help),
                mixerHandler = new MixerRecipeHandler(help),
                outgasserHandler = new RBMKOutgasserRecipeHandler(help),
                reformingHandler = new ReformingHandler(help),
                solidificationHandler = new SolidificationHandler(help),
                solderingStationHandler = new SolderingStationRecipeHandler(help),
                arcWelderRecipeHandler = new ArcWelderRecipeHandler(help),
                rotaryFurnaceRecipeHandler = new RotaryFurnaceRecipeHandler(help),
                new CentrifugeRecipeHandler(help),
                new GasCentrifugeRecipeHandler(help),
                new BreederRecipeHandler(help),
                new CyclotronRecipeHandler(help),
                new TransmutationRecipeHandler(help),
                new CMBFurnaceRecipeHandler(help),
                new WasteDrumRecipeHandler(help),
                new StorageDrumRecipeHandler(help),
                new FluidRecipeHandler(help),
                new SILEXRecipeHandler(help),
                new SILEXRadioRecipeHandler(help),
                new SILEXMicroRecipeHandler(help),
                new SILEXIrRecipeHandler(help),
                new SILEXVisibleRecipeHandler(help),
                new SILEXUVRecipeHandler(help),
                new SILEXXRayRecipeHandler(help),
                new SILEXGammaRecipeHandler(help),
                new SILEXDigammaRecipeHandler(help),
                new RBMKFuelRecipeHandler(help),
                new FusionRecipeHandler(help),
                new HadronRecipeHandler(help),
                new DFCRecipeHandler(help),
                new BookRecipeHandler(help));
    }


    private static final ISubtypeRegistry.ISubtypeInterpreter metadataFluidContainerInterpreter = stack -> {
        FluidType type = Fluids.fromID(stack.getMetadata());
        if (type != null && type != Fluids.NONE && FluidContainerRegistry.getFluidContainer(stack) != null) {
            return type.getTranslationKey();
        }
        return "";
    };

	@Override
	public void registerSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
		if(!GeneralConfig.jei)
			return;
        subtypeRegistry.registerSubtypeInterpreter(ModItems.cell, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_tank_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_barrel_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_tank_lead_full, metadataFluidContainerInterpreter);
        subtypeRegistry.registerSubtypeInterpreter(ModItems.canister_generic, metadataFluidContainerInterpreter);
		subtypeRegistry.registerSubtypeInterpreter(ModItems.missile_custom, (ItemStack stack) -> ModItems.missile_custom.getTranslationKey() + "w" +
                ItemCustomMissile.readFromNBT(stack, "warhead") + "f" + ItemCustomMissile.readFromNBT(stack, "fuselage") + "s" +
                ItemCustomMissile.readFromNBT(stack, "stability") + "t" + ItemCustomMissile.readFromNBT(stack, "thruster"));
        subtypeRegistry.registerSubtypeInterpreter(ModItems.fluid_icon, stack -> {
            FluidType fluidType = ItemFluidIcon.getFluidType(stack);
            if (fluidType != null) {
                return fluidType.getTranslationKey();
            }
            return "";
        });
	}

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
    }
}
