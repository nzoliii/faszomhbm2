package com.hbm.main;

import com.hbm.animloader.AnimationWrapper.EndResult;
import com.hbm.animloader.AnimationWrapper.EndType;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.DigammaMatter;
import com.hbm.blocks.generic.BMPowerBox;
import com.hbm.blocks.generic.BlockModDoor;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.machine.BlockSeal;
import com.hbm.blocks.machine.WatzPump;
import com.hbm.blocks.machine.rbmk.RBMKDebrisRadiating;
import com.hbm.blocks.network.energy.BlockCableGauge.TileEntityCableGauge;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.*;
import com.hbm.entity.grenade.*;
import com.hbm.entity.item.EntityFireworks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.entity.logic.*;
import com.hbm.entity.missile.*;
import com.hbm.entity.mob.*;
import com.hbm.entity.mob.botprime.EntityBOTPrimeBody;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.entity.mob.sodtekhnologiyah.EntityBallsOTronSegment;
import com.hbm.entity.particle.*;
import com.hbm.entity.projectile.*;
import com.hbm.handler.*;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFFFluidDuct;
import com.hbm.items.machine.ItemFluidIDMulti;
import com.hbm.items.special.ItemAutogen;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.items.special.ItemDepletedFuel;
import com.hbm.items.tool.ItemGasCanister;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.RecoilHandler;
import com.hbm.lib.RefStrings;
import com.hbm.particle.*;
import com.hbm.particle.bfg.*;
import com.hbm.particle.bullet_hit.ParticleBloodParticle;
import com.hbm.particle.bullet_hit.ParticleBulletImpact;
import com.hbm.particle.bullet_hit.ParticleHitDebris;
import com.hbm.particle.bullet_hit.ParticleSmokeAnim;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.particle_instanced.InstancedParticleRenderer;
import com.hbm.particle_instanced.ParticleContrailInstanced;
import com.hbm.particle_instanced.ParticleExSmokeInstanced;
import com.hbm.particle_instanced.ParticleRocketFlameInstanced;
import com.hbm.render.GLCompat;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.HbmAnimations.Animation;
import com.hbm.render.anim.HbmAnimations.BlenderAnimation;
import com.hbm.render.entity.*;
import com.hbm.render.entity.effect.*;
import com.hbm.render.entity.item.RenderMovingItem;
import com.hbm.render.entity.item.RenderMovingPackage;
import com.hbm.render.entity.missile.*;
import com.hbm.render.entity.mob.*;
import com.hbm.render.entity.projectile.*;
import com.hbm.render.factories.*;
import com.hbm.render.item.*;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.weapon.*;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.modelrenderer.EgonBackpackRenderer;
import com.hbm.render.tileentity.*;
import com.hbm.render.util.RenderInfoSystemLegacy;
import com.hbm.render.util.RenderOverhead;
import com.hbm.sound.AudioWrapper;
import com.hbm.sound.AudioWrapperClient;
import com.hbm.sound.AudioWrapperClientStartStop;
import com.hbm.sound.SoundLoopCrucible;
import com.hbm.tileentity.TileEntityDoorGeneric;
import com.hbm.tileentity.TileEntityKeypadBase;
import com.hbm.tileentity.TileEntitySlidingBlastDoorKeypad;
import com.hbm.tileentity.bomb.*;
import com.hbm.tileentity.deco.*;
import com.hbm.tileentity.machine.*;
import com.hbm.tileentity.machine.oil.*;
import com.hbm.tileentity.machine.rbmk.*;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.network.energy.TileEntityCableBaseNT;
import com.hbm.tileentity.network.energy.TileEntityPylon;
import com.hbm.tileentity.network.energy.TileEntityPylonLarge;
import com.hbm.tileentity.network.energy.TileEntitySubstation;
import com.hbm.tileentity.turret.*;
import com.hbm.util.BobMathUtil;
import com.hbm.util.I18nUtil;
import com.hbm.wiaj.cannery.Jars;
import java.awt.*;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.*;
import net.minecraft.client.particle.ParticleFirework.Spark;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import paulscode.sound.SoundSystemConfig;

public class ClientProxy extends ServerProxy {

    public static final ModelResourceLocation IRRELEVANT_MRL = new ModelResourceLocation("hbm:placeholdermodel", "inventory");
    public static final FloatBuffer AUX_GL_BUFFER = GLAllocation.createDirectFloatBuffer(16);
    public static final FloatBuffer AUX_GL_BUFFER2 = GLAllocation.createDirectFloatBuffer(16);
    //Drillgon200: Will I ever figure out how to write better code than this?
    public static final List<Runnable> deferredRenderers = new ArrayList<>();
    public static KeyBinding jetpackActivate;
    public static KeyBinding jetpackHover;
    public static KeyBinding jetpackHud;
    public static KeyBinding fsbFlashlight;
    public static KeyBinding craneUpKey;
    public static KeyBinding craneDownKey;
    public static KeyBinding craneLeftKey;
    public static KeyBinding craneRightKey;
    public static KeyBinding craneLoadKey;
    //Drillgon200: This is stupid, but I'm lazy
    public static boolean renderingConstant = false;
    public static int boxcarCalllist;
    public RenderInfoSystemLegacy theInfoSystem = new RenderInfoSystemLegacy();
    private HashMap<Integer, Long> vanished = new HashMap<>();

    public static void registerItemRenderer(Item i, TileEntityItemStackRenderer render, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        i.setTileEntityItemStackRenderer(render);
        ModEventHandlerClient.swapModels(i, reg);
    }

    @Override
    public File getDataDir() {
        return Minecraft.getMinecraft().gameDir;
    }

    @Override
    public void init(FMLInitializationEvent evt) {
        ItemDepletedFuel.registerColorHandlers();
        ItemBedrockOreNew.registerColorHandlers();
        ItemFFFluidDuct.registerColorHandlers();
        ItemGasCanister.registerColorHandler();
        ItemAutogen.registerColorHandlers();
    }

    @Override
    public void registerRenderInfo() {
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
            Minecraft.getMinecraft().getFramebuffer().enableStencil();

        MinecraftForge.EVENT_BUS.register(new ModEventHandlerClient());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerRenderer());

        MinecraftForge.EVENT_BUS.register(theInfoSystem);
        FMLCommonHandler.instance().bus().register(theInfoSystem);

        HbmShaderManager.loadShaders();

        jetpackActivate = new KeyBinding("key.jetpack_activate", KeyConflictContext.IN_GAME, Keyboard.KEY_J, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackActivate);
        jetpackHover = new KeyBinding("key.jetpack_hover", KeyConflictContext.IN_GAME, Keyboard.KEY_H, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackHover);
        jetpackHud = new KeyBinding("key.jetpack_hud", KeyConflictContext.IN_GAME, Keyboard.KEY_U, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackHud);
        fsbFlashlight = new KeyBinding("key.fsb_flashlight", KeyConflictContext.IN_GAME, Keyboard.KEY_NUMPAD6, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(fsbFlashlight);

        HbmKeybinds.register();
        Jars.initJars();

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePress.class, new RenderPress());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAmmoPress.class, new RenderAmmoPress());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeatBoilerIndustrial.class, new RenderIndustrialBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssembler.class, new RenderAssembler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssemfac.class, new RenderAssemfac());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTestRender.class, new RenderTestRender());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineChemplant.class, new RenderChemplant());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineChemfac.class, new RenderChemfac());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAutosaw.class, new RenderAutosaw());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMixer.class, new RenderMixer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeMan.class, new RenderNukeMan());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeFleija.class, new RenderNukeFleija());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCableBaseNT.class, new RenderCable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCableGauge.class, new RenderCableGauge());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlock.class, new RenderDecoBlock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPad.class, new RenderLaunchPadTier1());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchPadLarge.class, new RenderLaunchPadLarge());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineEPress.class, new RenderEPress());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPylon.class, new RenderPylon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPylonLarge.class, new RenderPylonLarge());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySubstation.class, new RenderSubstation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCentrifuge.class, new RenderCentrifuge());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGasCent.class, new RenderGasCent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineUF6Tank.class, new RenderUF6Tank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePuF6Tank.class, new RenderPuF6Tank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRailgun.class, new RenderRailgun());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFluidTank.class, new RenderFluidTank());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRefinery.class, new RenderRefinery());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLiquefactor.class, new RenderLiquefactor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSolidifier.class, new RenderSolidifier());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCyclotron.class, new RenderCyclotron());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBroadcaster.class, new RenderBroadcaster());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGeiger.class, new RenderGeiger());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDeuteriumTower.class, new RenderDeuteriumTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityVaultDoor.class, new RenderVaultDoor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlastDoor.class, new RenderBlastDoor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadGen.class, new RenderRadGen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOilWell.class, new RenderDerrick());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpjack.class, new RenderPumpjack());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFrackingTower.class, new RenderFrackingTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCatalyticCracker.class, new RenderCatalyticCracker());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineVacuumDistill.class, new RenderVacuumDistill());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCatalyticReformer.class, new RenderCatalyticReformer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCoker.class, new RenderCoker());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHydrotreater.class, new RenderHydrotreater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineGasFlare.class, new RenderGasFlare());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineExcavator.class, new RenderExcavator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineTurbofan.class, new RenderTurbofan());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadiobox.class, new RenderRadiobox());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRadioRec.class, new RenderRadioRec());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStructureMarker.class, new RenderStructureMarker());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeGadget.class, new RenderNukeGadget());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeBoy.class, new RenderNukeBoy());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeMike.class, new RenderNukeMike());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeTsar.class, new RenderNukeTsar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukePrototype.class, new RenderNukePrototype());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeSolinium.class, new RenderNukeSolinium());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeN2.class, new RenderNukeN2());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeCustom.class, new RenderNukeCustom());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBombMulti.class, new RenderBombMulti());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrashedBomb.class, new RenderCrashedBomb());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLandmine.class, new RenderLandmine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMissileAssembly.class, new RenderMissileAssembly());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCompactLauncher.class, new RenderCompactLauncher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMultiblock.class, new RenderMultiblock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaunchTable.class, new RenderLaunchTable());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzLauncher.class, new RenderSoyuzLauncher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSatDock.class, new RenderSatDock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySawmill.class, new RenderSawmill());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStirling.class, new RenderStirling());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrucible.class, new RenderCrucible());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorResearch.class, new RenderSmallReactor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineArcWelder.class, new RenderArcWelder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineStrandCaster.class, new RenderStrandCaster());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSolderingStation.class, new RenderSolderingStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryMold.class, new RenderFoundry());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryBasin.class, new RenderFoundry());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFoundryChannel.class, new RenderFoundryChannel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityReactorZirnox.class, new RenderZirnox());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityZirnoxDestroyed.class, new RenderZirnoxDestroyed());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityForceField.class, new RenderMachineForceField());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarNT.class, new RenderRadar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarLarge.class, new RenderRadarLarge());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRadarScreen.class, new RenderRadarScreen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineDrain.class, new RenderDrain());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoPoleTop.class, new RenderPoleTop());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoPoleSatelliteReceiver.class, new RenderPoleSatelliteReceiver());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityObjTester.class, new RenderObjTester());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDecoBlockAlt.class, new RenderDecoBlockAlt());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipeBaseNT.class, new RenderFluidDuctMk2<>());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraneSplitter.class, new RenderCraneSplitter());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRotaryFurnace.class, new RenderRotaryFurnace());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBarrel.class, new RenderFluidBarrel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTesla.class, new RenderTesla());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreEmitter.class, new RenderCoreComponent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreReceiver.class, new RenderCoreComponent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreInjector.class, new RenderCoreComponent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCoreStabilizer.class, new RenderCoreComponent());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCore.class, new RenderCore());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzCapsule.class, new RenderCapsule());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySoyuzStruct.class, new RenderSoyuzMultiblock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMiningLaser.class, new RenderLaserMiner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityITERStruct.class, new RenderITERMultiblock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityNukeBalefire.class, new RenderNukeFstbmb());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineCrystallizer.class, new RenderCrystallizer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMicrowave.class, new RenderMicrowave());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineRTG.class, new RenderRTG());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineMiniRTG.class, new RenderRTG());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityITER.class, new RenderITER());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFENSU.class, new RenderFENSU());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePlasmaHeater.class, new RenderPlasmaHeater());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpSteam.class, new RenderPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachinePumpElectric.class, new RenderPump());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPlasmaStruct.class, new RenderPlasmaMultiblock());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineSteamEngine.class, new RenderSteamEngine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLargeTurbine.class, new RenderBigTurbine());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineReactorBreeding.class, new RenderBreeder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCharger.class, new RenderCharger());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySlidingBlastDoor.class, new RenderSlidingBlastDoor());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypadBase.class, new RenderKeypadBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySlidingBlastDoorKeypad.class, new RenderKeypadBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlackBook.class, new RenderBookCrafting());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOreSlopper.class, new RenderOreSlopper());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarBoiler.class, new RenderSolarBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOrbitalStation.class, new RenderOrbitalStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeatBoiler.class, new RenderHeatBoiler());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolarMirror.class, new RenderSolarMirror());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySiloHatch.class, new RenderSiloHatch());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpinnyLight.class, new RenderSpinnyLight());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityControlPanel.class, new RenderControlPanel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDemonLamp.class, new RenderDemonLamp());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretArty.class, new RenderTurretArty());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHIMARS.class, new RenderTurretHIMARS());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretChekhov.class, new RenderTurretChekhov());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretFriendly.class, new RenderTurretFriendly());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretJeremy.class, new RenderTurretJeremy());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretTauon.class, new RenderTurretTauon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHoward.class, new RenderTurretHoward());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretHowardDamaged.class, new RenderTurretHowardDamaged());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretMaxwell.class, new RenderTurretMaxwell());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretFritz.class, new RenderTurretFritz());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretBrandon.class, new RenderTurretBrandon());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTurretRichard.class, new RenderTurretRichard());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStorageDrum.class, new RenderStorageDrum());
        //RBMK
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKControlManual.class, new RenderRBMKControlRod());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKControlAuto.class, new RenderRBMKControlRod());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKConsole.class, new RenderRBMKConsole());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKCraneConsole.class, new RenderRBMKCraneConsole());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKAbsorber.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKBlank.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKBoiler.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKModerator.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKOutgasser.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKStorage.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKCooler.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKHeater.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKReflector.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKRod.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRBMKRodReaSim.class, new RenderRBMKLid());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBMPowerBox.class, new RenderBMPowerBox());

        //WATZ
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatz.class, new RenderWatz());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatzStruct.class, new RenderWatzMultiblock());
        ClientRegistry.bindTileEntitySpecialRenderer(WatzPump.TileEntityWatzPump.class, new RenderWatzPump());

        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineBAT9000.class, new RenderBAT9000());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineOrbus.class, new RenderOrbus());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChungus.class, new RenderChungus());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySpacer.class, new RenderSpacer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineFractionTower.class, new RenderFractionTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTowerSmall.class, new RenderSmallTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTowerLarge.class, new RenderLargeTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySILEX.class, new RenderSILEX());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFEL.class, new RenderFEL());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterFirebox.class, new RenderFirebox());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterOven.class, new RenderHeatingOven());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterOilburner.class, new RenderOilburner());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterRadioThermal.class, new RenderRadioThermal());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterElectric.class, new RenderHeaterElectric());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityHeaterHeatex.class, new RenderHeaterHeatex());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnaceIron.class, new RenderFurnaceIron());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFurnaceSteel.class, new RenderFurnaceSteel());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDoorGeneric.class, new RenderDoorGeneric());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHTR3.class, new RenderHTR3());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineHTRF4.class, new RenderHTRF4());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineLPW2.class, new RenderLPW2());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityXenonThruster.class, new RenderXenonThruster());

        RenderingRegistry.registerEntityRenderingHandler(EntityDSmokeFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.d_smoke1, ModItems.d_smoke2, ModItems.d_smoke3, ModItems.d_smoke4, ModItems.d_smoke5, ModItems.d_smoke6, ModItems.d_smoke7, ModItems.d_smoke8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityOrangeFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.orange1, ModItems.orange2, ModItems.orange3, ModItems.orange4, ModItems.orange5, ModItems.orange6, ModItems.orange7, ModItems.orange8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityCloudFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.cloud1, ModItems.cloud2, ModItems.cloud3, ModItems.cloud4, ModItems.cloud5, ModItems.cloud6, ModItems.cloud7, ModItems.cloud8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityPinkCloudFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.pc1, ModItems.pc2, ModItems.pc3, ModItems.pc4, ModItems.pc5, ModItems.pc6, ModItems.pc7, ModItems.pc8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityChlorineFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.chlorine1, ModItems.chlorine2, ModItems.chlorine3, ModItems.chlorine4, ModItems.chlorine5, ModItems.chlorine6, ModItems.chlorine7, ModItems.chlorine8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityTaintedCreeper.class, new RenderTaintedCreeperFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityNuclearCreeper.class, new RenderNuclearCreeperFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityFalloutRain.class, new RenderFalloutRainFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityNukeTorex.class, RenderTorex.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySmokeFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.smoke1, ModItems.smoke2, ModItems.smoke3, ModItems.smoke4, ModItems.smoke5, ModItems.smoke6, ModItems.smoke7, ModItems.smoke8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityBSmokeFX.class, new MultiCloudRendererFactory(new Item[]{ModItems.b_smoke1, ModItems.b_smoke2, ModItems.b_smoke3, ModItems.b_smoke4, ModItems.b_smoke5, ModItems.b_smoke6, ModItems.b_smoke7, ModItems.b_smoke8}));
        RenderingRegistry.registerEntityRenderingHandler(EntityShrapnel.class, new ShrapnelRendererFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntitySSmokeFX.class, new RenderSSmokeFactory(ModItems.nuclear_waste));
        RenderingRegistry.registerEntityRenderingHandler(EntityRubble.class, new RenderRubbleFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityBurningFOEQ.class, new RenderBurningFOEQFactory());
        RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleijaRainbow.class, RenderCloudRainbow.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosiveBeam.class, RenderBeam5.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCloudFleija.class, RenderCloudFleija.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBullet.class, RenderBullet.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGasFlameFX.class, GasFlameRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, RenderRocket.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityFire.class, RenderFireProjectile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityAAShell.class, RenderAAShell.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBomber.class, RenderBomber.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier1.EntityMissileGeneric.class, RenderMissileGeneric.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRocketHoming.class, RenderSRocket.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTSmokeFX.class, TSmokeRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBoxcar.class, RenderBoxcar.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBombletZeta.class, RenderBombletZeta.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier1.EntityMissileIncendiary.class, RenderMissileIncendiary.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier1.EntityMissileCluster.class, RenderMissileCluster.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier1.EntityMissileBunkerBuster.class, RenderMissileBunkerBuster.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier2.EntityMissileStrong.class, RenderMissileStrong.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier2.EntityMissileIncendiaryStrong.class, RenderMissileIncendiaryStrong.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier2.EntityMissileClusterStrong.class, RenderMissileClusterStrong.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier2.EntityMissileBusterStrong.class, RenderMissileBusterStrong.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier2.EntityMissileEMPStrong.class, RenderMissileEMPStrong.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityEMP.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileBurst.class, RenderMissileBurst.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileInferno.class, RenderMissileInferno.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileRain.class, RenderMissileRain.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileDrill.class, RenderMissileDrill.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier4.EntityMissileN2.class, RenderMissileNuclear.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier4.EntityMissileNuclear.class, RenderMissileNuclear.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier4.EntityMissileMirv.class, RenderMissileNuclear.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileEndo.class, RenderMissileEndo.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier3.EntityMissileExo.class, RenderMissileExo.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBombletTheta.class, RenderBombletTheta.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBombletSelena.class, RenderBombletSelena.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier4.EntityMissileDoomsday.class, RenderMissileDoomsday.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier0.EntityMissileTaint.class, RenderMissileTaint.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier0.EntityMissileMicro.class, RenderMissileMicro.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier0.EntityMissileBHole.class, RenderMissileBHole.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, RenderBlackHole.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier0.EntityMissileSchrabidium.class, RenderMissileSchrabidium.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityEMPBlast.class, RenderEMPBlast.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier0.EntityMissileEMP.class, RenderMissileEMP.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileAntiBallistic.class, RenderMissileAB.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileStealth.class, RenderMissileStealth.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBooster.class, RenderBoosterMissile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCarrier.class, RenderCarrierMissile.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBulletBase.class, RenderBulletMk2.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityDuchessGambit.class, RenderBoat.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySparkBeam.class, RenderBeam4.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityModBeam.class, RenderBeam6.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityVortex.class, RenderBlackHole.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRagingVortex.class, RenderBlackHole.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityNukeExplosionMK5.class, RenderNukeMK5.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityFalloutUnderGround.class, RenderFalloutUnderground.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMiniNuke.class, RenderMiniNuke.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMiniMIRV.class, RenderMiniMIRV.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBaleflare.class, RenderBaleflare.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRainbow.class, RenderRainbow.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityPlasmaBeam.class, RenderBeam.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLN2.class, RenderLN2.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCombineBall.class, (RenderManager man) -> {
            return new RenderSnowball<EntityCombineBall>(man, ModItems.energy_ball, Minecraft.getMinecraft().getRenderItem()) {
                @Override
                public void doRender(EntityCombineBall entity, double x, double y, double z, float entityYaw, float partialTicks) {
                    GlStateManager.disableLighting();
                    super.doRender(entity, x, y, z, entityYaw, partialTicks);
                    GlStateManager.enableLighting();
                }
            };
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityDischarge.class, ElectricityRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeGeneric.class, (RenderManager man) -> {
            return new RenderSnowball<EntityGrenadeGeneric>(man, ModItems.grenade_generic, Minecraft.getMinecraft().getRenderItem());
        });
        registerGrenadeRenderer(EntityGrenadeStrong.class, ModItems.grenade_strong);
        registerGrenadeRenderer(EntityGrenadeFrag.class, ModItems.grenade_frag);
        registerGrenadeRenderer(EntityGrenadeFire.class, ModItems.grenade_fire);
        registerGrenadeRenderer(EntityGrenadeCluster.class, ModItems.grenade_cluster);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeFlare.class, RenderFlare.FACTORY);
        registerGrenadeRenderer(EntityGrenadeElectric.class, ModItems.grenade_electric);
        registerGrenadeRenderer(EntityGrenadePoison.class, ModItems.grenade_poison);
        registerGrenadeRenderer(EntityGrenadeGas.class, ModItems.grenade_gas);
        RenderingRegistry.registerEntityRenderingHandler(EntitySchrab.class, RenderFlare.FACTORY_SCHRAB);
        registerGrenadeRenderer(EntityGrenadeSchrabidium.class, ModItems.grenade_schrabidium);
        registerGrenadeRenderer(EntityGrenadePulse.class, ModItems.grenade_pulse);
        registerGrenadeRenderer(EntityGrenadePlasma.class, ModItems.grenade_plasma);
        registerGrenadeRenderer(EntityGrenadeTau.class, ModItems.grenade_tau);
        registerGrenadeRenderer(EntityGrenadeCloud.class, ModItems.grenade_cloud);
        registerGrenadeRenderer(EntityGrenadePC.class, ModItems.grenade_pink_cloud);
        registerGrenadeRenderer(EntityGrenadeSmart.class, ModItems.grenade_smart);
        registerGrenadeRenderer(EntityGrenadeMIRV.class, ModItems.grenade_mirv);
        registerGrenadeRenderer(EntityGrenadeBreach.class, ModItems.grenade_breach);
        registerGrenadeRenderer(EntityGrenadeBurst.class, ModItems.grenade_burst);
        registerGrenadeRenderer(EntityGrenadeLemon.class, ModItems.grenade_lemon);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeMk2.class, RenderGrenade.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeASchrab.class, RenderGrenade.FACTORY);
        registerGrenadeRenderer(EntityGrenadeZOMG.class, ModItems.grenade_zomg);
        registerGrenadeRenderer(EntityGrenadeSolinium.class, ModItems.grenade_solinium);
        registerGrenadeRenderer(EntityGrenadeShrapnel.class, ModItems.grenade_shrapnel);
        registerGrenadeRenderer(EntityGrenadeBlackHole.class, ModItems.grenade_black_hole);
        registerGrenadeRenderer(EntityGrenadeGascan.class, ModItems.grenade_gascan);
        registerGrenadeRenderer(EntityGrenadeNuke.class, ModItems.grenade_nuke);
        registerGrenadeRenderer(EntityGrenadeNuclear.class, ModItems.grenade_nuclear);
        registerGrenadeRenderer(EntityGrenadeIFGeneric.class, ModItems.grenade_if_generic);
        registerGrenadeRenderer(EntityGrenadeIFHE.class, ModItems.grenade_if_he);
        registerGrenadeRenderer(EntityGrenadeIFBouncy.class, ModItems.grenade_if_bouncy);
        registerGrenadeRenderer(EntityGrenadeIFSticky.class, ModItems.grenade_if_sticky);
        registerGrenadeRenderer(EntityGrenadeIFImpact.class, ModItems.grenade_if_impact);
        registerGrenadeRenderer(EntityGrenadeIFIncendiary.class, ModItems.grenade_if_incendiary);
        registerGrenadeRenderer(EntityGrenadeIFToxic.class, ModItems.grenade_if_toxic);
        registerGrenadeRenderer(EntityGrenadeIFConcussion.class, ModItems.grenade_if_concussion);
        registerGrenadeRenderer(EntityGrenadeIFBrimstone.class, ModItems.grenade_if_brimstone);
        registerGrenadeRenderer(EntityGrenadeIFMystery.class, ModItems.grenade_if_mystery);
        registerGrenadeRenderer(EntityGrenadeIFSpark.class, ModItems.grenade_if_spark);
        registerGrenadeRenderer(EntityGrenadeIFHopwire.class, ModItems.grenade_if_hopwire);
        registerGrenadeRenderer(EntityGrenadeIFNull.class, ModItems.grenade_if_null);
        RenderingRegistry.registerEntityRenderingHandler(EntityRailgunBlast.class, RenderTom.RAIL_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityNukeExplosionMK3.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGasFX.class, GasRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityOilSpill.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityOilSpillFX.class, SpillRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCloudSolinium.class, RenderCloudSolinium.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityNukeExplosionPlus.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityFallingNuke.class, RenderFallingNuke.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileCustom.class, RenderMissileCustom.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityDeathBlast.class, RenderDeathBlast.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMinerRocket.class, RenderMinerRocket.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, RenderMeteor.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBobmazon.class, RenderBobmazon.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCyberCrab.class, RenderCyberCrab.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityHunterChopper.class, RenderHunterChopper.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityChopperMine.class, RenderChopperMine.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityWaterSplash.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMinerBeam.class, RenderBeam3.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLaserBeam.class, RenderBeam2.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMIRV.class, RenderMirv.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBuilding.class, RenderBuilding.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTaintCrab.class, RenderTaintCrab.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTeslaCrab.class, RenderTeslaCrab.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTom.class, RenderTom.TOM_FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityTomBlast.class, RenderEmpty.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySoyuzCapsule.class, RenderSoyuzCapsule.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySoyuz.class, RenderSoyuz.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLaser.class, RenderLaser.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMovingItem.class, RenderMovingItem.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMovingPackage.class, RenderMovingPackage.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCloudTom.class, RenderCloudTom.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMaskMan.class, RenderMaskMan.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBallsOTronSegment.class, RenderBalls.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBOTPrimeHead.class, RenderWormHead.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBOTPrimeBody.class, RenderWormBody.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityDuck.class, RenderDuck.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGlowingOne.class, RenderGlowingOne.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityBeamVortex.class, RenderVortexBeam.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityQuackos.class, RenderQuacc.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityFBI.class, RenderFBI.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRADBeast.class, RenderRADBeast.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityFireworks.class, RenderShrapnel.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityRBMKDebris.class, RenderRBMKDebris.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityZirnoxDebris.class, RenderZirnoxDebris.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityArtilleryShell.class, RenderArtilleryShell.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityArtilleryRocket.class, RenderArtilleryRocket.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySawblade.class, RenderSawblade.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntitySpear.class, RenderSpear.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissileTier4.EntityMissileVolcano.class, RenderMissileNuclear.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityUFO.class, RenderUFO.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityQuasar.class, RenderQuasar.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityMist.class, RenderMist.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityCog.class, RenderCog.FACTORY);


        ModelLoader.setCustomStateMapper(ModBlocks.toxic_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.radwater_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_bunker, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_metal, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_office, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.mud_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.schrabidic_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.corium_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.volcanic_lava_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.seal_controller, new StateMap.Builder().ignore(BlockSeal.ACTIVATED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.ntm_dirt, new StateMap.Builder().ignore(BlockDirt.SNOWY).ignore(BlockDirt.VARIANT).build());
        ModelLoader.setCustomStateMapper(ModBlocks.brick_jungle_trap, new StateMap.Builder().ignore(TrappedBrick.TYPE).build());
        ModelLoader.setCustomStateMapper(ModBlocks.stone_porous, new StateMap.Builder().ignore(BlockStone.VARIANT).build());
        ModelLoader.setCustomStateMapper(ModBlocks.volcano_core, new StateMap.Builder().ignore(BlockDummyable.META).build());
        ModelLoader.setCustomStateMapper(ModBlocks.bm_power_box, new StateMap.Builder().ignore(BMPowerBox.FACING, BMPowerBox.IS_ON).build());
        //Drillgon200: This can't be efficient, but eh.
        for (Block b : ModBlocks.ALL_BLOCKS) {
            if (b instanceof BlockDummyable || b instanceof RBMKDebrisRadiating || b instanceof DigammaMatter)
                ModelLoader.setCustomStateMapper(b, new StateMap.Builder().ignore(BlockDummyable.META).build());
        }
    }

    private <E extends Entity> void registerGrenadeRenderer(Class<E> clazz, Item grenade) {
        RenderingRegistry.registerEntityRenderingHandler(clazz, (RenderManager man) -> {
            return new RenderSnowball<E>(man, grenade, Minecraft.getMinecraft().getRenderItem());
        });
    }

    @Override
    public void registerMissileItems(IRegistry<ModelResourceLocation, IBakedModel> reg) {
        MissilePart.registerAllParts();

        //Iterator<Map.Entry<Integer, MissilePart>> it = MissilePart.parts.entrySet().iterator();
        MissilePart.parts.values().forEach(part -> {
            registerItemRenderer(part.part, new ItemRenderMissilePart(part), reg);
        });

        registerItemRenderer(ModItems.missile_custom, new ItemRenderMissile(), reg);

        ItemRenderMissileGeneric.init();
        registerItemRenderer(ModItems.missile_taint, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_micro, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_bhole, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_schrabidium, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_emp, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_generic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_incendiary, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_buster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_anti_ballistic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ABM), reg);
        registerItemRenderer(ModItems.missile_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_incendiary_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_cluster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_buster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_emp_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_burst, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_inferno, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_rain, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_drill, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_nuclear, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_nuclear_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_volcano, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_n2, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_endo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL), reg);
        registerItemRenderer(ModItems.missile_exo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL), reg);
        registerItemRenderer(ModItems.missile_doomsday, new ItemRenderMissileGeneric(RenderMissileType.TYPE_DOOMSDAY), reg);
        registerItemRenderer(ModItems.missile_carrier, new ItemRenderMissileGeneric(RenderMissileType.TYPE_CARRIER), reg);
    }

    @Override
    public void registerTileEntitySpecialRenderer() {

    }

    @Override
    public void particleControl(double x, double y, double z, int type) {
        World world = Minecraft.getMinecraft().world;

        switch (type) {
            case 0:

                for (int i = 0; i < 10; i++) {
                    Particle smoke = new ParticleCloud.Factory().createParticle(EnumParticleTypes.CLOUD.getParticleID(), world, x + world.rand.nextGaussian(), y + world.rand.nextGaussian(), z + world.rand.nextGaussian(), 0.0, 0.0, 0.0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
                }
                break;

            case 1:
                Particle s = new ParticleCloud.Factory().createParticle(EnumParticleTypes.CLOUD.getParticleID(), world, x, y, z, 0.0, 0.1, 0.0);
                Minecraft.getMinecraft().effectRenderer.addEffect(s);

                break;

            case 2:
                if (GeneralConfig.instancedParticles) {
                    ParticleContrailInstanced contrail2 = new ParticleContrailInstanced(world, x, y, z);
                    InstancedParticleRenderer.addParticle(contrail2);
                } else {
                    ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
                }
                break;
            case 3: //Rad Fog

                ParticleRadiationFog fog = new ParticleRadiationFog(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fog);
                break;
            case 4:
                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + 1.1, z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + 1.1, z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x - 0.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x - 0.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + 1.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + 1.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z - 0.1, 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z - 0.1, 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + 1.1, 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + 1.1, 0.0, 0.0, 0.0);
                break;
        }
    }

    //version 2, now with strings!
    @Override
    public void spawnParticle(double x, double y, double z, String type, float args[]) {
        World world = Minecraft.getMinecraft().world;
        TextureManager man = Minecraft.getMinecraft().renderEngine;

        if ("launchsmoke".equals(type)) {
            ParticleSmokePlume contrail = new ParticleSmokePlume(man, world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exKerosene".equals(type)) {
            ParticleContrail contrail = new ParticleContrailKerosene(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exSolid".equals(type)) {
            ParticleContrail contrail = new ParticleContrailSolid(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exHydrogen".equals(type)) {
            ParticleContrail contrail = new ParticleContrailHydrogen(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exBalefire".equals(type)) {
            ParticleContrail contrail = new ParticleContrailBalefire(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exDark".equals(type)) {
            ParticleContrail contrail = new ParticleContrailDark(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("bfg_fire".equals(type)) {
            BlockPos pos = new BlockPos(x, y, z);
            int fireAge = (int) args[0];
            if (fireAge >= 0) {
                if (fireAge >= 1 && fireAge <= 40) {
                    Vec3 attractionPoint = Vec3.createVectorHelper(pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 60);
                    for (int i = 0; i < world.rand.nextInt(6); i++) {
                        float randPosX = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -10, 10);
                        float randPosY = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -10, 10);
                        float randPosZ = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, 0, 10);
                        float randMotionX = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionY = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionZ = world.rand.nextFloat() * 0.4F - 0.2F;
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGParticle(world, pos.getX() + 0.5 + randPosX, pos.getY() + 24 + randPosY, pos.getZ() + 0.5 - 74 + +randPosZ, randMotionX, randMotionY, randMotionZ, attractionPoint));
                    }
                }

                if (fireAge >= 1 && fireAge <= 12 && fireAge % 3 == 0) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }
                if (fireAge >= 28 && fireAge <= 32 && fireAge % 2 == 0) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }
                if (fireAge > 32 && fireAge <= 52) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }

                if (fireAge == 10) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGPrefire(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 21));
                }

                if (fireAge == 58) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGBeam(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 25));
                }
                if (fireAge >= 58 && fireAge <= 70) {
                    for (int i = 0; i < 20; i++) {
                        float randPosX = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -5, 5);
                        float randPosY = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -5, 5);
                        float randPosZ = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, 0, -200);
                        float randMotionX = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionY = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionZ = world.rand.nextFloat() - 5.4F - 4F;
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGParticle(world, pos.getX() + 0.5 + randPosX, pos.getY() + 24 + randPosY, pos.getZ() + 0.5 - 44 + +randPosZ, randMotionX, randMotionY, randMotionZ, null));
                    }
                }
                if (fireAge == 58 || fireAge == 64) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGSmoke(world, pos.getX() + 0.5, pos.getY() + 23, pos.getZ() + 0.5 - 55));
                }
                if (fireAge == 58 || fireAge == 68 || fireAge == 83) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGRing(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 55));
                }
                if (fireAge == 60) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGShockwave(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 55, 2, 30, 1, 0.95F));
                }
                if (fireAge == 65) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGShockwave(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 65, 5, 25, 0.6F, 0.98F));
                }


            }
        }
    }

    //Drillgon200: Sending whole tag compounds to spawn particles can't be efficient...
    @SuppressWarnings("deprecation")
    //mk3, only use this one
    @Override
    public void effectNT(NBTTagCompound data) {
        World world = Minecraft.getMinecraft().world;
        if (world == null)
            return;
        EntityPlayer player = Minecraft.getMinecraft().player;
        int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        Random rand = world.rand;
        String type = data.getString("type");
        double x = data.getDouble("posX");
        double y = data.getDouble("posY");
        double z = data.getDouble("posZ");

        if (ParticleCreators.particleCreators.containsKey(type)) {
            ParticleCreators.particleCreators.get(type).makeParticle(world, player,
                    Minecraft.getMinecraft().renderEngine, rand, x, y, z, data);
            return;
        }
        switch (type) {
            case "missileContrail" -> {
                if (Vec3.createVectorHelper(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

                float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");

                /*ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0, 0, 0, scale);
                contrail.motionX = mX;
                contrail.motionY = mY;
                contrail.motionZ = mZ;
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);*/

                ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z).setScale(scale);
                fx.setMotion(mX, mY, mZ);
                if (data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
            case "smoke" -> {
                String mode = data.getString("mode");
                int count = Math.max(1, data.getInteger("count"));

                switch (mode) {
                    case "cloud" -> {
                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                double motionY = rand.nextGaussian() * (1 + (count / 100D));
                                double motionX = rand.nextGaussian() * (1 + (count / 150D));
                                double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                                if (rand.nextBoolean()) motionY = Math.abs(motionY);
                                fx.setMotion(motionX, motionY, motionZ);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                double motionY = rand.nextGaussian() * (1 + (count / 100D));
                                double motionX = rand.nextGaussian() * (1 + (count / 150D));
                                double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                                if (rand.nextBoolean()) motionY = Math.abs(motionY);
                                fx.setMotion(motionX, motionY, motionZ);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "radial" -> {
                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                                        rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                                        rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "radialDigamma" -> {
                        Vec3 vec = Vec3.createVectorHelper(2, 0, 0);
                        vec.rotateAroundY(rand.nextFloat() * (float) Math.PI * 2F);

                        for (int i = 0; i < count; i++) {
                            ParticleDigammaSmoke fx = new ParticleDigammaSmoke(world, x, y, z);
                            fx.motion((float) vec.xCoord, 0, (float) vec.zCoord);
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                            vec.rotateAroundY((float) Math.PI * 2F / (float) count);
                        }
                    }
                    case "shock" -> {
                        double strength = data.getDouble("strength");

                        Vec3 vec = Vec3.createVectorHelper(strength, 0, 0);
                        vec.rotateAroundY(rand.nextInt(360));

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(vec.xCoord, 0, vec.zCoord);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(vec.xCoord, 0, vec.zCoord);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateAroundY(360F / count);
                        }
                    }
                    case "shockRand" -> {
                        double strength = data.getDouble("strength");

                        Vec3 vec = Vec3.createVectorHelper(strength, 0, 0);
                        vec.rotateAroundY(rand.nextInt(360));
                        double r;

                        for (int i = 0; i < count; i++) {
                            r = rand.nextDouble();
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(vec.xCoord * r, 0, vec.zCoord * r);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(vec.xCoord * r, 0, vec.zCoord * r);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateAroundY(360F / count);
                        }
                    }
                    case "wave" -> {
                        double strength = data.getDouble("range");

                        Vec3 vec = Vec3.createVectorHelper(strength, 0, 0);

                        for (int i = 0; i < count; i++) {

                            vec.rotateAroundY((float) Math.toRadians(rand.nextFloat() * 360F));

                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x + vec.xCoord, y,
                                        z + vec.zCoord);
                                fx.setMotion(0, 0, 0);
                                fx.setMaxAge(50);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x + vec.xCoord, y, z + vec.zCoord);
                                fx.setMotion(0, 0, 0);
                                fx.setMaxAge(50);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateAroundY(360F / count);
                        }
                    }
                }
            }
            case "network" -> {
                ParticleDebug debug = null;
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");

                switch (data.getString("mode")) {
                    case "power" -> debug = new ParticleDebug(world, x, y, z);
                    case "fluid" -> {
                        int color = data.getInteger("color");
                        debug = new ParticleDebug(world, x, y, z, mX, mY, mZ, color);
                    }
                }
                Minecraft.getMinecraft().effectRenderer.addEffect(debug);
            }
            case "exhaust" -> {
                String mode = data.getString("mode");

                switch (mode) {
                    case "soyuz" -> {
                        if (Vec3.createVectorHelper(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
                            return;

                        int count = Math.max(1, data.getInteger("count"));
                        double width = data.getDouble("width");

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                                        x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                                fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleRocketFlame fx = new ParticleRocketFlame(world,
                                        x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                                fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "meteor" -> {
                        if (Vec3.createVectorHelper(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
                            return;

                        int count = Math.max(1, data.getInteger("count"));
                        double width = data.getDouble("width");

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                                        x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                                        z + rand.nextGaussian() * width);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleRocketFlame fx = new ParticleRocketFlame(world,
                                        x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                                        z + rand.nextGaussian() * width);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                }
            }
            case "ufo" -> {
                if (GeneralConfig.instancedParticles) {
                    ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world, x, y, z);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "haze" -> {
                ParticleHaze fog = new ParticleHaze(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fog);
            }
            case "plasmablast" -> {
                ParticlePlasmaBlast cloud = new ParticlePlasmaBlast(world, x, y, z, data.getFloat("r"),
                        data.getFloat("g"), data.getFloat("b"), data.getFloat("pitch"), data.getFloat("yaw"));
                cloud.setScale(data.getFloat("scale"));
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            case "justTilt" -> {
                player.hurtTime = player.maxHurtTime = data.getInteger("time");
                player.attackedAtYaw = 0F;
            }
            case "properJolt" -> {
                player.hurtTime = data.getInteger("time");
                player.maxHurtTime = data.getInteger("maxTime");
                player.attackedAtYaw = 0F;
            }
            case "marker" -> {
                int color = data.getInteger("color");
                String label = data.getString("label");
                int expires = data.getInteger("expires");
                double dist = data.getDouble("dist");

                RenderOverhead.queuedMarkers.put(new BlockPos(x, y, z),
                        new RenderOverhead.Marker(color).setDist(dist).setExpire(expires > 0 ?
                                System.currentTimeMillis() + expires : 0).withLabel(label.isEmpty() ? null : label));
            }
            case "casing" -> {
                CasingEjector ejector = CasingEjector.fromId(data.getInteger("ej"));
                if (ejector == null) return;
                SpentCasing casingConfig = SpentCasing.fromName((data.getString("name")));
                if (casingConfig == null) return;

                for (int i = 0; i < ejector.getAmount(); i++) {
                    ejector.spawnCasing(Minecraft.getMinecraft().renderEngine, casingConfig, world, x, y, z,
                            data.getFloat("pitch"), data.getFloat("yaw"), data.getBoolean("crouched"));
                }
            }
            case "foundry" -> {
                int color = data.getInteger("color");
                byte dir = data.getByte("dir");
                float length = data.getFloat("len");
                float base = data.getFloat("base");
                float offset = data.getFloat("off");

                ParticleFoundry sploosh = new ParticleFoundry(world, x, y, z, color, dir, length, base, offset);
                Minecraft.getMinecraft().effectRenderer.addEffect(sploosh);
            }
            case "fireworks" -> {
                int color = data.getInteger("color");
                char c = (char) data.getInteger("char");

                ParticleLetter fx = new ParticleLetter(world, x, y, z, color, c);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                for (int i = 0; i < 50; i++) {
                    Spark blast = new ParticleFirework.Spark(world, x, y, z,
                            0.4 * world.rand.nextGaussian(),
                            0.4 * world.rand.nextGaussian(),
                            0.4 * world.rand.nextGaussian(), Minecraft.getMinecraft().effectRenderer);
                    blast.setColor(color);
                    Minecraft.getMinecraft().effectRenderer.addEffect(blast);
                }
            }
            case "vomit" -> {
                Entity e = world.getEntityByID(data.getInteger("entity"));
                int count = data.getInteger("count");
                if (e instanceof EntityLivingBase) {

                    double ix = e.posX;
                    double iy = e.posY - e.getYOffset() + e.getEyeHeight() + (e instanceof EntityPlayer ? -0.5 : 0);
                    double iz = e.posZ;

                    Vec3d vec = e.getLookVec();

                    String mode = data.getString("mode");
                    for (int i = 0; i < count; i++) {
                        switch (mode) {
                            case "normal" -> {
                                int stateId =
                                        Block.getStateId(Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR
                                                , rand.nextBoolean() ? EnumDyeColor.LIME : EnumDyeColor.GREEN));
                                Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                        , (vec.z + rand.nextGaussian() * 0.2) * 0.2, stateId);
                                fx.setMaxAge(150 + rand.nextInt(50));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                            case "blood" -> {
                                Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                        , (vec.z + rand.nextGaussian() * 0.2) * 0.2,
                                        Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState()));
                                fx.setMaxAge(150 + rand.nextInt(50));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                            case "smoke" -> {
                                Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.1) * 0.05,
                                        (vec.y + rand.nextGaussian() * 0.1) * 0.05,
                                        (vec.z + rand.nextGaussian() * 0.1) * 0.05);
                                fx.setMaxAge(10 + rand.nextInt(10));
                                fx.particleScale *= 0.2F;
                                ((ParticleSmokeNormal) fx).smokeParticleScale = fx.particleScale;
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                }
            }
            case "sweat" -> {
                Entity e = world.getEntityByID(data.getInteger("entity"));
                Block b = Block.getBlockById(data.getInteger("block"));
                int meta = data.getInteger("meta");

                if (e instanceof EntityLivingBase) {

                    for (int i = 0; i < data.getInteger("count"); i++) {

                        double ix =
                                e.getEntityBoundingBox().minX - 0.2 + (e.getEntityBoundingBox().maxX - e.getEntityBoundingBox().minX + 0.4) * rand.nextDouble();
                        double iy =
                                e.getEntityBoundingBox().minY + (e.getEntityBoundingBox().maxY - e.getEntityBoundingBox().minY + 0.2) * rand.nextDouble();
                        double iz =
                                e.getEntityBoundingBox().minZ - 0.2 + (e.getEntityBoundingBox().maxZ - e.getEntityBoundingBox().minZ + 0.4) * rand.nextDouble();


                        Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz, 0, 0, 0,
                                Block.getStateId(b.getStateFromMeta(meta)));
                        fx.setMaxAge(150 + rand.nextInt(50));

                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                    }
                }
            }
            case "radiation" -> {
                for (int i = 0; i < data.getInteger("count"); i++) {

                    Particle flash = new ParticleSuspendedTown.Factory().createParticle(-1, world,
                            player.posX + rand.nextGaussian() * 4,
                            player.posY + rand.nextGaussian() * 2,
                            player.posZ + rand.nextGaussian() * 4,
                            0, 0, 0);

                    flash.setRBGColorF(0F, 0.75F, 1F);
                    flash.motionX = rand.nextGaussian();
                    flash.motionY = rand.nextGaussian();
                    flash.motionZ = rand.nextGaussian();
                    Minecraft.getMinecraft().effectRenderer.addEffect(flash);
                }
            }
            case "radiationfog" -> {
                ParticleRadiationFog fog = new ParticleRadiationFog(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fog);
            }
            case "vanillaburst" -> {
                double motion = data.getDouble("motion");

                for (int i = 0; i < data.getInteger("count"); i++) {

                    double mX = rand.nextGaussian() * motion;
                    double mY = rand.nextGaussian() * motion;
                    double mZ = rand.nextGaussian() * motion;

                    Particle fx = null;
                    String mode = data.getString("mode");
                    switch (mode) {
                        case "flame" -> fx = new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        case "cloud" -> fx = new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        case "reddust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.0F, 0.0F,
                                0.0F);
                        case "bluedust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F,
                                1F);
                        case "greendust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F,
                                0.1F);
                        case "blockdust" -> {
                            Block b = Block.getBlockById(data.getInteger("block"));
                            fx = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ,
                                    Block.getStateId(b.getDefaultState()));
                            fx.setMaxAge(50 + rand.nextInt(50));
                        }
                    }

                    if (fx != null)
                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "vanillaExt" -> {

                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");

                Particle fx = null;
                String mode = data.getString("mode");
                switch (mode) {
                    case "flame" -> fx = new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "smoke" -> fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "volcano" -> {
                        fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        ((ParticleSmokeNormal) fx).smokeParticleScale = 100f;
                        fx.setMaxAge(200 + rand.nextInt(50));
                        fx.canCollide = false;
                        fx.motionX = rand.nextGaussian() * 0.2;
                        fx.motionY = 2.5 + rand.nextDouble();
                        fx.motionZ = rand.nextGaussian() * 0.2;
                    }
                    case "cloud" -> fx = new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "reddust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, (float) mX,
                            (float) mY, (float) mZ);
                    case "bluedust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F, 1F);
                    case "greendust" -> fx = new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F, 0.1F);
                    case "largeexplode" -> {
                        fx = new ParticleExplosionLarge.Factory().createParticle(-1, world, x, y, z, data.getFloat(
                                "size"), 0.0F, 0.0F);
                        float r = 1.0F - rand.nextFloat() * 0.2F;
                        fx.setRBGColorF(1F * r, 0.9F * r, 0.5F * r);

                        for (int i = 0; i < data.getByte("count"); i++) {
                            ParticleExplosion sec =
                                    (ParticleExplosion) new ParticleExplosion.Factory().createParticle(-1, world, x, y, z,
                                            0.0F, 0.0F, 0.0F);
                            float r2 = 1.0F - rand.nextFloat() * 0.5F;
                            sec.setRBGColorF(0.5F * r2, 0.5F * r2, 0.5F * r2);
                            sec.multipleParticleScaleBy(i + 1);
                            Minecraft.getMinecraft().effectRenderer.addEffect(sec);
                        }
                    }
                    case "townaura" -> {
                        fx = new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0, 0);
                        float color = 0.5F + rand.nextFloat() * 0.5F;
                        fx.setRBGColorF(0.8F * color, 0.9F * color, 1.0F * color);
                        fx.motionX = mX;
                        fx.motionY = mY;
                        fx.motionZ = mZ;
                    }
                    case "blockdust" -> {
                        Block b = Block.getBlockById(data.getInteger("block"));
                        int id = Block.getStateId(b.getDefaultState());
                        fx = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ, id);
                        fx.setMaxAge(10 + rand.nextInt(20));
                    }
                }

                if (fx != null)
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
            case "spark" -> {
                String mode = data.getString("mode");
                double dirX = data.getDouble("dirX");
                double dirY = data.getDouble("dirY");
                double dirZ = data.getDouble("dirZ");
                float width = data.hasKey("width") ? data.getFloat("width") : 0.025F;
                float length = data.hasKey("length") ? data.getFloat("length") : 1.0F;
                float randLength = data.hasKey("randLength") ? data.getFloat("randLength") - length : 0;
                float gravity = data.hasKey("gravity") ? data.getFloat("gravity") : 9.81F * 0.01F;
                int lifetime = data.hasKey("lifetime") ? data.getInteger("lifetime") : 100;
                int randLifeTime = data.hasKey("randLifetime") ? data.getInteger("randLifetime") - lifetime : lifetime;
                float velocityRand = data.hasKey("randomVelocity") ? data.getFloat("randomVelocity") : 1.0F;
                float r = data.hasKey("r") ? data.getFloat("r") : 1.0F;
                float g = data.hasKey("g") ? data.getFloat("g") : 1.0F;
                float b = data.hasKey("b") ? data.getFloat("b") : 1.0F;
                float a = data.hasKey("a") ? data.getFloat("a") : 1.0F;
                if (mode.equals("coneBurst")) {
                    float angle = data.hasKey("angle") ? data.getFloat("angle") : 10;
                    float randAngle = data.hasKey("randAngle") ? data.getFloat("randAngle") - angle : 0;
                    int count = data.hasKey("count") ? data.getInteger("count") : 1;
                    for (int i = 0; i < count; i++) {
                        //Gets a random vector rotated within a cone and then rotates it to the particle data's direction
                        //Create a new vector and rotate it randomly about the x-axis within the angle specified, then rotate that by random degrees to get the random cone vector
                        Vec3 up = Vec3.createVectorHelper(0, 1, 0);
                        up.rotateAroundX((float) Math.toRadians(rand.nextFloat() * (angle + rand.nextFloat() * randAngle)));
                        up.rotateAroundY((float) Math.toRadians(rand.nextFloat() * 360));
                        //Finds the angles for the particle direction and rotate our random cone vector to it.
                        Vec3 direction = Vec3.createVectorHelper(dirX, dirY, dirZ);
                        Vec3 angles = BobMathUtil.getEulerAngles(direction);
                        Vec3 newDirection = Vec3.createVectorHelper(up.xCoord, up.yCoord, up.zCoord);
                        newDirection.rotateAroundX((float) Math.toRadians(angles.yCoord - 90));
                        newDirection.rotateAroundY((float) Math.toRadians(angles.xCoord));
                        //Multiply it by the original vector's length to ensure it has the right magnitude
                        newDirection = newDirection.mult((float) direction.length() + rand.nextFloat() * velocityRand);
                        Particle fx = new ParticleSpark(world, x, y, z, length + rand.nextFloat() * randLength, width
                                , lifetime + rand.nextInt(randLifeTime), gravity).color(r, g, b, a).motion((float) newDirection.xCoord,
                                (float) newDirection.yCoord, (float) newDirection.zCoord);
                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                    }
                }
            }
            case "hadron" -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z));
            case "schrabfog" -> {
                ParticleSuspendedTown flash =
                        (ParticleSuspendedTown) new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0,
                                0);
                flash.setRBGColorF(0F, 1F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(flash);
            }
            case "rift" -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRift(world, x, y, z));
            case "rbmkflame" -> {
                int maxAge = data.getInteger("maxAge");
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKFlame(world, x, y, z, maxAge));
            }
            case "rbmkmush" -> {
                float scale = data.getFloat("scale");
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKMush(world, x, y, z, scale));
            }
            case "tower" -> {
                if (particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
                    ParticleCoolingTower fx = new ParticleCoolingTower(world, x, y, z);
                    fx.setLift(data.getFloat("lift"));
                    fx.setBaseScale(data.getFloat("base"));
                    fx.setMaxScale(data.getFloat("max"));
                    fx.setLife(data.getInteger("life") / (particleSetting + 1));
                    if (data.hasKey("noWind")) fx.noWind();
                    if (data.hasKey("strafe")) fx.setStrafe(data.getFloat("strafe"));
                    if (data.hasKey("alpha")) fx.alphaMod(data.getFloat("alpha"));

                    if (data.hasKey("color")) {
                        Color color = new Color(data.getInteger("color"));
                        fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "jetpack" -> {
                Entity ent = world.getEntityByID(data.getInteger("player"));

                if (ent instanceof EntityPlayer p) {

                    Vec3 vec = Vec3.createVectorHelper(0, 0, -0.25);
                    Vec3 offset = Vec3.createVectorHelper(0.125, 0, 0);
                    float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                    vec.rotateAroundY(angle);
                    offset.rotateAroundY(angle);

                    double ix = p.posX + vec.xCoord;
                    double iy = p.posY + p.eyeHeight - 1;
                    double iz = p.posZ + vec.zCoord;
                    double ox = offset.xCoord;
                    double oz = offset.zCoord;

                    double moX = 0;
                    double moY = 0;
                    double moZ = 0;

                    int mode = data.getInteger("mode");

                    if (mode == 0) {
                        moY -= 0.2;
                    }

                    if (mode == 1) {
                        Vec3d look = p.getLookVec();

                        moX -= look.x * 0.1D;
                        moY -= look.y * 0.1D;
                        moZ -= look.z * 0.1D;
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                            world, ix + ox, iy, iz + oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                            world, ix - ox, iy, iz - oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix + ox, iy,
                            iz + oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix - ox, iy,
                            iz - oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
                }
            }
            case "bimpact" -> {
                Type hitType = Type.values()[data.getByte("hitType")];
                Vec3d normal = new Vec3d(data.getFloat("nX"), data.getFloat("nY"), data.getFloat("nZ"));
                if (hitType == Type.BLOCK) {
                    IBlockState state = Block.getBlockById(data.getInteger("block")).getStateFromMeta(data.getByte(
                            "meta"));
                    Material mat = state.getMaterial();
                    float r = 1;
                    float g = 1;
                    float b = 1;
                    float scale = 1;
                    float randMotion = 0.2F;
                    int count = 10;
                    int smokeCount = 3;
                    int smokeScale = 5;
                    int smokeLife = 15;
                    if (mat == Material.IRON) {
                        world.playSound(x, y, z, HBMSoundHandler.hit_metal, SoundCategory.BLOCKS, 1,
                                0.9F + world.rand.nextFloat() * 0.2F, false);
                    } else {
                        world.playSound(x, y, z, HBMSoundHandler.hit_dirt, SoundCategory.BLOCKS, 1,
                                0.7F + world.rand.nextFloat() * 0.3F, false);
                    }
                    if (mat == Material.ROCK || mat == Material.GROUND || mat == Material.GRASS || mat == Material.WOOD || mat == Material.LEAVES || mat == Material.SAND) {
                        ResourceLocation tex = ResourceManager.rock_fragments;
                        if (mat == Material.WOOD) {
                            tex = ResourceManager.wood_fragments;
                        } else if (mat == Material.LEAVES) {
                            tex = ResourceManager.twigs_and_leaves;
                            smokeLife = 5;
                            smokeScale = 10;
                            smokeCount = 2;
                        }
                        if (mat == Material.GROUND || mat == Material.GRASS) {
                            r = 0.8F;
                            g = 0.5F;
                            b = 0.3F;
                            scale = 0.6F;
                            count = 40;
                        }
                        if (mat == Material.SAND) {
                            r = 1F;
                            g = 0.9F;
                            b = 0.6F;
                            scale = 0.1F;
                            randMotion = 0.5F;
                            count = 100;
                            smokeCount = 5;
                        }
                        for (int i = 0; i < count; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 45, world.rand);
                            dir = dir.scale(0.1F + world.rand.nextFloat() * randMotion);
                            Vec3d offset = normal.scale(0.2F);
                            ParticleHitDebris particle = new ParticleHitDebris(world, x + offset.x, y + offset.y,
                                    z + offset.z, tex, world.rand.nextInt(16), scale, 40 + world.rand.nextInt(20));
                            offset = offset.scale(1);
                            particle.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            particle.color(r, g, b);
                            ParticleBatchRenderer.addParticle(particle);
                        }
                        if (mat == Material.WOOD) {
                            tex = ResourceManager.wood_fragments;
                            r = 0.8F;
                            g = 0.5F;
                            b = 0.3F;
                        }
                        if (mat == Material.LEAVES) {
                            r = 0.2F;
                            g = 0.8F;
                            b = 0.4F;
                        }
                    }
                    if (mat != Material.LEAVES) {
                        ParticleBulletImpact impact = new ParticleBulletImpact(world, x + normal.x * 0.01F,
                                y + normal.y * 0.01F, z + normal.z * 0.01F, 0.1F, 60 + world.rand.nextInt(20), normal);
                        impact.color(r, g, b);
                        ParticleBatchRenderer.addParticle(impact);
                    }
                    if (mat == Material.SAND) {
                        r *= 1.5F;
                        g *= 1.5F;
                        b *= 1.5F;
                    }
                    if (mat == Material.IRON) {
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setString("type", "spark");
                        nbt.setString("mode", "coneBurst");
                        nbt.setDouble("posX", x);
                        nbt.setDouble("posY", y);
                        nbt.setDouble("posZ", z);
                        nbt.setDouble("dirX", normal.x * 0.6F);
                        nbt.setDouble("dirY", normal.y * 0.6F);
                        nbt.setDouble("dirZ", normal.z * 0.6F);
                        nbt.setFloat("r", 0.8F);
                        nbt.setFloat("g", 0.6F);
                        nbt.setFloat("b", 0.5F);
                        nbt.setFloat("a", 1.5F);
                        nbt.setInteger("lifetime", 1 + rand.nextInt(2));
                        nbt.setFloat("width", 0.03F);
                        nbt.setFloat("length", 0.3F);
                        nbt.setFloat("randLength", 0.6F);
                        nbt.setFloat("gravity", 0.1F);
                        nbt.setFloat("angle", 60F);
                        nbt.setInteger("count", 2 + rand.nextInt(2));
                        nbt.setFloat("randomVelocity", 0.3F);
                        effectNT(nbt);
                    } else {
                        for (int i = 0; i < smokeCount; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                            dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                            ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                    smokeScale + world.rand.nextFloat() * smokeScale, 1, smokeLife);
                            smoke.color(r * 0.5F, g * 0.5F, b * 0.5F);
                            smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            ParticleBatchRenderer.addParticle(smoke);
                        }
                    }

                } else if (hitType == Type.ENTITY) {
                    world.playSound(x, y, z, HBMSoundHandler.hit_flesh, SoundCategory.BLOCKS, 1,
                            0.8F + world.rand.nextFloat() * 0.4F, false);
                    Vec3d bulletDirection = new Vec3d(data.getFloat("dirX"), data.getFloat("dirY"), data.getFloat(
                            "dirZ"));
                    if (GeneralConfig.bloodFX) {
                        for (int i = 0; i < 2; i++) {
                            int age = 10 + world.rand.nextInt(5);
                            ParticleBloodParticle blood = new ParticleBloodParticle(world, x, y, z,
                                    world.rand.nextInt(9), 1 + world.rand.nextFloat() * 3,
                                    0.5F + world.rand.nextFloat() * 0.5F, age);
                            blood.color(0.5F, 0F, 0F);
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 70, world.rand);
                            dir = dir.scale(0.05F + world.rand.nextFloat() * 0.25);
                            if (i > 0) {
                                dir = BobMathUtil.randVecInCone(bulletDirection.normalize(), 20, world.rand);
                                dir = dir.scale(1F + world.rand.nextFloat());
                                blood.setMaxAge((int) (age * 0.75F));
                            }
                            blood.motion((float) dir.x, (float) dir.y + 0.1F, (float) dir.z);
                            ParticleBatchRenderer.addParticle(blood);
                        }
                        for (int i = 0; i < 3; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                            dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                            ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                    3 + world.rand.nextFloat() * 3, 1, 10);
                            smoke.color(0.4F, 0, 0);
                            smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            ParticleBatchRenderer.addParticle(smoke);
                        }
                    }
                }
            }
            case "vanilla" -> {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");
                world.spawnParticle(EnumParticleTypes.getByName(data.getString("mode")), x, y, z, mX, mY, mZ);
            }
            case "anim" -> {
                EnumHand hand = EnumHand.values()[data.getInteger("hand")];
                int slot = player.inventory.currentItem;
                if (hand == EnumHand.OFF_HAND) {
                    slot = 9;
                }
                String name = data.getString("name");
                String mode = data.getString("mode");

                switch (name) {
                    case "crucible" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.crucible_equip,
                                            new EndResult(EndType.STAY));
                            case "crucible" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("GUARD_ROT", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 0))
                                                .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 800))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 1, 50)));

                                HbmAnimations.hotbar[slot] =
                                        new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                System.currentTimeMillis(), animation);
                            }
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                            case "cSwing" -> {
                                if (HbmAnimations.getRelevantTransformation("SWING_ROT", hand)[0] == 0) {

                                    int offset = rand.nextInt(80) - 20;

                                    BusAnimation animation = new BusAnimation()
                                            .addBus("SWING_ROT", new BusAnimationSequence()
                                                    .addKeyframe(new BusAnimationKeyframe(60 - offset, 60 - offset,
                                                            -55, 75))
                                                    .addKeyframe(new BusAnimationKeyframe(60 + offset, 60 - offset,
                                                            -45, 150))
                                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)))
                                            .addBus("SWING_TRANS", new BusAnimationSequence()
                                                    .addKeyframe(new BusAnimationKeyframe(-0, -10, 0, 75))
                                                    .addKeyframe(new BusAnimationKeyframe(0, -10, 0, 150))
                                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));

                                    //Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord
                                    // .getMasterRecord(HBMSoundHandler.cSwing, 0.8F + player.getRNG().nextFloat() *
                                    // 0.2F));

                                    if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                        HbmAnimations.hotbar[slot].animation = animation;
                                        HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                    } else {
                                        HbmAnimations.hotbar[slot] =
                                                new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                        System.currentTimeMillis(), animation);
                                    }
                                }
                            }
                        }
                    }
                    case "hs_sword" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.hs_sword_equip,
                                            new EndResult(EndType.STAY));
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                        }
                    }
                    case "hf_sword" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.hf_sword_equip,
                                            new EndResult(EndType.STAY));
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                        }
                    }
                }

                if ("generic".equals(mode)) {
                    ItemStack stack = player.getHeldItem(hand);

                    if (!stack.isEmpty() && stack.getItem() instanceof IAnimatedItem item) {
                        BusAnimation anim = item.getAnimation(data, stack);

                        if (anim != null) {
                            HbmAnimations.hotbar[slot] =
                                    new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), anim);
                        }
                    }
                }
            }
            case "tau" -> {
                for (int i = 0; i < data.getByte("count"); i++)
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHbmSpark(world, x, y, z,
                            rand.nextGaussian() * 0.05, 0.05, rand.nextGaussian() * 0.05));
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z));
            }
            case "vanish" -> this.vanish(data.getInteger("ent"));
            case "giblets" -> {
                int ent = data.getInteger("ent");
                this.vanish(ent);
                Entity e = world.getEntityByID(ent);

                if (e == null)
                    return;

                float width = e.width;
                float height = e.height;
                int gW = (int) (width / 0.25F);
                int gH = (int) (height / 0.25F);

                boolean blowMeIntoTheGodDamnStratosphere = rand.nextInt(15) == 0;
                double mult = 1D;

                if (blowMeIntoTheGodDamnStratosphere)
                    mult *= 10;

                for (int i = -(gW / 2); i <= gW; i++) {
                    for (int j = 0; j <= gH; j++) {
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiblet(world, x, y, z,
                                rand.nextGaussian() * 0.25 * mult, rand.nextDouble() * mult,
                                rand.nextGaussian() * 0.25 * mult));
                    }
                }
            }
            case "sound" -> {
                String mode = data.getString("mode");
                if (mode.equals("crucible_loop")) {
                    int id = data.getInteger("playerId");
                    Entity e = world.getEntityByID(id);
                    if (e instanceof EntityPlayer) {
                        Minecraft.getMinecraft().getSoundHandler().playSound(new SoundLoopCrucible((EntityPlayer) e));
                    }
                }
            }
        }
    }

    public void vanish(int ent) {
        vanished.put(ent, System.currentTimeMillis() + 2000);
    }

    @Override
    public boolean isVanished(Entity e) {

        if (e == null)
            return false;

        if (!this.vanished.containsKey(e.getEntityId()))
            return false;

        return this.vanished.get(e.getEntityId()) > System.currentTimeMillis();
    }

    @Override
    public boolean getIsKeyPressed(EnumKeybind key) {

        switch (key) {
            case JETPACK:
                return Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            case TOGGLE_JETPACK:
                return HbmKeybinds.jetpackKey.isKeyDown();
            case TOGGLE_HEAD:
                return HbmKeybinds.hudKey.isKeyDown();
            case RELOAD:
                return HbmKeybinds.reloadKey.isKeyDown();
            case CRANE_UP:
                return HbmKeybinds.craneUpKey.isKeyDown();
            case CRANE_DOWN:
                return HbmKeybinds.craneDownKey.isKeyDown();
            case CRANE_LEFT:
                return HbmKeybinds.craneLeftKey.isKeyDown();
            case CRANE_RIGHT:
                return HbmKeybinds.craneRightKey.isKeyDown();
            case CRANE_LOAD:
                return HbmKeybinds.craneLoadKey.isKeyDown();
        }

        return false;
    }

    @Override
    public EntityPlayer me() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void setRecoil(float rec) {
        RecoilHandler.verticalVelocity = rec;
    }

    @Override
    public void spawnSFX(World world, double posX, double posY, double posZ, int type, Vec3 payload) {
        int pow = 250;
        float angle = 25;
        float base = 0.5F;
        for (int i = 0; i < pow; i++) {

            float momentum = base * world.rand.nextFloat();
            float sway = (pow - i) / (float) pow;
            Vec3 vec = Vec3.createVectorHelper(((Vec3) payload).xCoord, ((Vec3) payload).yCoord, ((Vec3) payload).zCoord);
            vec.rotateAroundZ((float) (angle * world.rand.nextGaussian() * sway * Math.PI / 180D));
            vec.rotateAroundY((float) (angle * world.rand.nextGaussian() * sway * Math.PI / 180D));

            ParticleFirework.Spark blast = new ParticleFirework.Spark(world, posX, posY, posZ, vec.xCoord * momentum, vec.yCoord * momentum, vec.zCoord * momentum, Minecraft.getMinecraft().effectRenderer);

            if (world.rand.nextBoolean())
                blast.setColor(0x0088EA);
            else
                blast.setColor(0x52A8E6);

            Minecraft.getMinecraft().effectRenderer.addEffect(blast);
        }
    }

    @Override
    public boolean opengl33() {
        return GLContext.getCapabilities().OpenGL33;
    }

    @Override
    public void checkGLCaps() {
        GLCompat.error = GLCompat.init();
        if (GLCompat.error.isEmpty()) {
            MainRegistry.logger.log(Level.INFO, "Advanced rendering fully supported");
        } else {
            MainRegistry.logger.log(Level.ERROR, "Advanced rendering not supported: " + GLCompat.error);
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent evt) {
        if (SoundSystemConfig.getNumberNormalChannels() < 128) {
            SoundSystemConfig.setNumberNormalChannels(128);
        }
        OBJLoader.INSTANCE.addDomain(RefStrings.MODID);

        ItemRenderLibrary.init();

        ModItems.redstone_sword.setTileEntityItemStackRenderer(ItemRedstoneSwordRender.INSTANCE);
        ModItems.assembly_template.setTileEntityItemStackRenderer(AssemblyTemplateRender.INSTANCE);
        ModItems.chemistry_template.setTileEntityItemStackRenderer(ChemTemplateRender.INSTANCE);
        ModItems.gun_b92.setTileEntityItemStackRenderer(ItemRenderGunAnim.INSTANCE);
        ModItems.fluid_tank_full.setTileEntityItemStackRenderer(new FluidTankRender());
        ModItems.fluid_barrel_full.setTileEntityItemStackRenderer(new FluidBarrelRender());
        ModItems.fluid_tank_lead_full.setTileEntityItemStackRenderer(new FluidLeadTankRender());
        ModItems.canister_generic.setTileEntityItemStackRenderer(FluidCanisterRender.INSTANCE);
        ModItems.gun_revolver_nightmare.setTileEntityItemStackRenderer(new ItemRenderRevolverNightmare());
        ModItems.gun_revolver_nightmare2.setTileEntityItemStackRenderer(new ItemRenderRevolverNightmare());
        ModItems.gun_revolver.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_gun_bright, ResourceManager.ff_gun_bright, ResourceManager.ff_wood));
        ModItems.gun_revolver_saturnite.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_saturnite, ResourceManager.ff_iron, ResourceManager.ff_wood));
        ModItems.gun_revolver_iron.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_iron, ResourceManager.ff_iron, ResourceManager.ff_wood));
        ModItems.gun_revolver_gold.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_gold, ResourceManager.ff_gold, ResourceManager.ff_wood_red));
        ModItems.gun_revolver_lead.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_lead, ResourceManager.ff_lead, ResourceManager.ff_gun_dark));
        ModItems.gun_revolver_schrabidium.setTileEntityItemStackRenderer(new ItemRenderWeaponFFColt(ResourceManager.ff_schrabidium, ResourceManager.ff_schrabidium, ResourceManager.ff_gun_dark));
        ModItems.gun_revolver_cursed.setTileEntityItemStackRenderer(new ItemRenderRevolverCursed());
        ModItems.gun_revolver_pip.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_revolver_nopip.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_revolver_blackjack.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_revolver_red.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_revolver_silver.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_lever_action.setTileEntityItemStackRenderer(new ItemRenderGunAnim2());
        ModItems.gun_spark.setTileEntityItemStackRenderer(new ItemRenderOverkill());
        ModItems.gun_b93.setTileEntityItemStackRenderer(new RenderGunB93());
        ModItems.gun_rpg.setTileEntityItemStackRenderer(new ItemRenderRpg());
        ModItems.gun_karl.setTileEntityItemStackRenderer(new ItemRenderRpg());
        ModItems.gun_panzerschreck.setTileEntityItemStackRenderer(new ItemRenderRpg());
        ModItems.gun_hk69.setTileEntityItemStackRenderer(new ItemRenderWeaponObj());
        ModItems.gun_deagle.setTileEntityItemStackRenderer(new ItemRenderWeaponObj());
        ModItems.gun_supershotgun.setTileEntityItemStackRenderer(new ItemRenderWeaponShotty());
        ModItems.gun_fatman.setTileEntityItemStackRenderer(new ItemRenderFatMan());
        ModItems.gun_proto.setTileEntityItemStackRenderer(new ItemRenderFatMan());
        ModItems.gun_mirv.setTileEntityItemStackRenderer(new ItemRenderMIRVLauncher());
        ModItems.gun_bf.setTileEntityItemStackRenderer(new ItemRenderBFLauncher());
        ModItems.gun_zomg.setTileEntityItemStackRenderer(new ItemRenderZOMG());
        ModItems.gun_xvl1456.setTileEntityItemStackRenderer(new ItemRenderXVL1456());
        ModItems.gun_hp.setTileEntityItemStackRenderer(new ItemRenderGunHP());
        ModItems.gun_defabricator.setTileEntityItemStackRenderer(new ItemRenderGunDefab());
        ModItems.gun_uboinik.setTileEntityItemStackRenderer(new ItemRenderUboinik());
        ModItems.gun_euthanasia.setTileEntityItemStackRenderer(new ItemRenderEuthanasia());
        ModItems.gun_stinger.setTileEntityItemStackRenderer(new ItemRenderStinger());
        ModItems.gun_skystinger.setTileEntityItemStackRenderer(new ItemRenderStinger());
        ModItems.gun_mp.setTileEntityItemStackRenderer(new ItemRenderMP());
        ModItems.gun_cryolator.setTileEntityItemStackRenderer(new ItemRenderCryolator());
        ModItems.gun_jack.setTileEntityItemStackRenderer(new ItemRenderGunJack());
        ModItems.gun_immolator.setTileEntityItemStackRenderer(new ItemRenderImmolator());
        ModItems.gun_osipr.setTileEntityItemStackRenderer(new ItemRenderOSIPR());
        ModItems.gun_emp.setTileEntityItemStackRenderer(new ItemRenderEMPRay());
        ModItems.gun_revolver_inverted.setTileEntityItemStackRenderer(new ItemRenderRevolverInverted());
        ModItems.gun_lever_action_sonata.setTileEntityItemStackRenderer(new ItemRenderGunSonata());
        ModItems.gun_bolt_action_saturnite.setTileEntityItemStackRenderer(new ItemRenderGunSaturnite());
        ModItems.gun_folly.setTileEntityItemStackRenderer(new ItemRenderFolly());
        ModItems.gun_dampfmaschine.setTileEntityItemStackRenderer(new ItemRenderBullshit());
        ModItems.gun_calamity.setTileEntityItemStackRenderer(new ItemRenderCalamity());
        ModItems.gun_calamity_dual.setTileEntityItemStackRenderer(new ItemRenderCalamity());
        ModItems.gun_minigun.setTileEntityItemStackRenderer(new ItemRenderMinigun());
        ModItems.gun_avenger.setTileEntityItemStackRenderer(new ItemRenderMinigun());
        ModItems.gun_lacunae.setTileEntityItemStackRenderer(new ItemRenderMinigun());
        ModItems.gun_bolt_action.setTileEntityItemStackRenderer(new ItemRenderGunAnim2());
        ModItems.gun_bolt_action_green.setTileEntityItemStackRenderer(new ItemRenderGunAnim2());
        ModItems.gun_lever_action_dark.setTileEntityItemStackRenderer(new ItemRenderGunAnim2());
        ModItems.gun_uzi.setTileEntityItemStackRenderer(new ItemRenderUzi());
        ModItems.gun_uzi_silencer.setTileEntityItemStackRenderer(new ItemRenderUzi());
        ModItems.gun_uzi_saturnite.setTileEntityItemStackRenderer(new ItemRenderUzi());
        ModItems.gun_uzi_saturnite_silencer.setTileEntityItemStackRenderer(new ItemRenderUzi());
        ModItems.gun_mp40.setTileEntityItemStackRenderer(new ItemRenderMP40());
        ModItems.cell.setTileEntityItemStackRenderer(new ItemRenderCell());
        ModItems.multitool_dig.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_silk.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_ext.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_miner.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_hit.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_beam.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_sky.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_mega.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_joule.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.multitool_decon.setTileEntityItemStackRenderer(new ItemRenderMultitool());
        ModItems.big_sword.setTileEntityItemStackRenderer(new ItemRenderBigSword());
        ModItems.shimmer_sledge.setTileEntityItemStackRenderer(new ItemRenderShim());
        ModItems.shimmer_axe.setTileEntityItemStackRenderer(new ItemRenderShim());
        ModItems.fluid_icon.setTileEntityItemStackRenderer(new ItemRenderFluidIcon());
        ModItems.gun_brimstone.setTileEntityItemStackRenderer(new ItemRenderObj());
        ModItems.stopsign.setTileEntityItemStackRenderer(new ItemRenderShim());
        ModItems.sopsign.setTileEntityItemStackRenderer(new ItemRenderShim());
        ModItems.gun_ks23.setTileEntityItemStackRenderer(new ItemRenderWeaponObj());
        ModItems.gun_flamer.setTileEntityItemStackRenderer(new ItemRenderWeaponObj());
        ModItems.gun_flechette.setTileEntityItemStackRenderer(new ItemRenderWeaponObj());
        ModItems.gun_quadro.setTileEntityItemStackRenderer(new ItemRenderWeaponQuadro());
        ModItems.gun_sauer.setTileEntityItemStackRenderer(new ItemRenderWeaponSauer());
        ModItems.chernobylsign.setTileEntityItemStackRenderer(new ItemRenderShim());
        Item.getItemFromBlock(ModBlocks.radiorec).setTileEntityItemStackRenderer(new ItemRendererMachine(1D));
        ModItems.gun_vortex.setTileEntityItemStackRenderer(new ItemRenderWeaponVortex());
        ModItems.gun_thompson.setTileEntityItemStackRenderer(new ItemRenderWeaponThompson());
        ModItems.wood_gavel.setTileEntityItemStackRenderer(new ItemRenderGavel());
        ModItems.lead_gavel.setTileEntityItemStackRenderer(new ItemRenderGavel());
        ModItems.diamond_gavel.setTileEntityItemStackRenderer(new ItemRenderGavel());
        ModItems.mese_gavel.setTileEntityItemStackRenderer(new ItemRenderGavel());
        ModItems.gun_bolter.setTileEntityItemStackRenderer(new ItemRenderWeaponBolter());
        ModItems.ingot_steel_dusted.setTileEntityItemStackRenderer(new ItemRendererHot());
        ModItems.ingot_chainsteel.setTileEntityItemStackRenderer(new ItemRendererHot());
        ModItems.ingot_meteorite.setTileEntityItemStackRenderer(new ItemRendererHot());
        ModItems.ingot_meteorite_forged.setTileEntityItemStackRenderer(new ItemRendererHot());
        ModItems.blade_meteorite.setTileEntityItemStackRenderer(new ItemRendererHot());
        ModItems.crucible.setTileEntityItemStackRenderer(new ItemRenderCrucible());
        ModItems.boltgun.setTileEntityItemStackRenderer(new ItemRenderBoltgun());
        ModItems.hs_sword.setTileEntityItemStackRenderer(new ItemRenderHSSword());
        ModItems.hf_sword.setTileEntityItemStackRenderer(new ItemRenderHFSword());
        ModItems.cc_plasma_gun.setTileEntityItemStackRenderer(new ItemRenderCCPlasmaCannon());
        ModItems.gun_egon.setTileEntityItemStackRenderer(new ItemRenderGunEgon());
        ModItems.jshotgun.setTileEntityItemStackRenderer(new ItemRenderJShotgun());
        ModItems.gun_ar15.setTileEntityItemStackRenderer(new ItemRenderWeaponAR15());

        ModItems.ammo_himars.setTileEntityItemStackRenderer(new ItemRenderTurretHIMARSAmmo());

        ModItems.meteorite_sword_seared.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 0.5F, 0.0F));
        ModItems.meteorite_sword_reforged.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.5F, 1.0F, 1.0F));
        ModItems.meteorite_sword_hardened.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.25F, 0.25F, 0.25F));
        ModItems.meteorite_sword_alloyed.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.0F, 0.5F, 1.0F));
        ModItems.meteorite_sword_machined.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 1.0F, 0.0F));
        ModItems.meteorite_sword_treated.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.5F, 1.0F, 0.5F));
        ModItems.meteorite_sword_etched.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 1.0F, 0.5F));
        ModItems.meteorite_sword_bred.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.5F, 0.5F, 0.0F));
        ModItems.meteorite_sword_irradiated.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.75F, 1.0F, 0.0F));
        ModItems.meteorite_sword_fused.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 0.0F, 0.5F));
        ModItems.meteorite_sword_baleful.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(0.0F, 1.0F, 0.0F));
        ModItems.meteorite_sword_warped.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 1.0F, 1.0F));
        ModItems.meteorite_sword_demonic.setTileEntityItemStackRenderer(new ItemRendererMeteorSword(1.0F, 0.0F, 0.0F));

        ModItems.ore_bedrock.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x575757, 0.2F));
        ModItems.ore_bedrock_centrifuged.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x676767, 0.25F));
        ModItems.ore_bedrock_cleaned.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x8E8E5B, 0.3F));
        ModItems.ore_bedrock_separated.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x7B7B7B, 0.35F));
        ModItems.ore_bedrock_deepcleaned.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x9A9D76, 0.4F));
        ModItems.ore_bedrock_purified.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x858689, 0.5F));
        ModItems.ore_bedrock_nitrated.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x95795A, 0.6F));
        ModItems.ore_bedrock_nitrocrystalline.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x79797F, 0.7F));
        ModItems.ore_bedrock_seared.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0xAAACAF, 0.8F));
        ModItems.ore_bedrock_exquisite.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x797D81, 0.9F));
        ModItems.ore_bedrock_perfect.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x6C6E70, 1F));
        ModItems.ore_bedrock_enriched.setTileEntityItemStackRenderer(new ItemRendererBedrockOre(0x55595D, 1F));
        ModItems.detonator_laser.setTileEntityItemStackRenderer(new ItemRendererDetonatorLaser());

        ModItems.forge_fluid_identifier.setTileEntityItemStackRenderer(new FFIdentifierRender());

        for (Entry<Item, ItemRenderBase> entry : ItemRenderLibrary.renderers.entrySet()) {

            entry.getKey().setTileEntityItemStackRenderer(entry.getValue());
        }
    }

    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat);
        audio.updatePosition(x, y, z);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSoundStartStop(World world, SoundEvent sound, SoundEvent start, SoundEvent stop, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClientStartStop audio = new AudioWrapperClientStartStop(world, sound, start, stop, volume, cat);
        audio.updatePosition(x, y, z);
        return audio;
    }

    @Override
    public void displayTooltipLegacy(String msg, int time, int id) {
        if (id != 0)
            this.theInfoSystem.push(new RenderInfoSystemLegacy.InfoEntry(msg, time), id);
        else
            this.theInfoSystem.push(new RenderInfoSystemLegacy.InfoEntry(msg, time));
    }

    @Override
    public void playSoundClient(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, category, volume, pitch, (float) x, (float) y, (float) z));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {

        boxcarCalllist = GL11.glGenLists(1);
        GL11.glNewList(boxcarCalllist, GL11.GL_COMPILE);
        ResourceManager.boxcar.renderAll();
        GL11.glEndList();
        ResourceManager.loadAnimatedModels();
        Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((p, r) -> {
            r.addLayer(new JetpackHandler.JetpackLayer());
            r.getMainModel().bipedBody.addChild(new EgonBackpackRenderer(r.getMainModel()));
        });

        ParticleRenderLayer.register();
        BobmazonOfferFactory.reset();
        BobmazonOfferFactory.init();
        ItemFluidIDMulti.registerItemColors();
    }

    @Override
    public void playSound(String sound, Object data) {

    }

    @Override
    public void displayTooltip(String msg) {
        if (msg.startsWith("chat."))
            msg = I18nUtil.resolveKey(msg);
        Minecraft.getMinecraft().ingameGUI.setOverlayMessage(msg, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public float partialTicks() {
        boolean paused = Minecraft.getMinecraft().isGamePaused();
        return paused ?  Minecraft.getMinecraft().renderPartialTicksPaused : Minecraft.getMinecraft().getRenderPartialTicks();
    }

}
