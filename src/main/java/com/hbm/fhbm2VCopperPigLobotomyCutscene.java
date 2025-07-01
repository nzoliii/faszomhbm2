package com.hbm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.util.*;

import org.lwjgl.opengl.GL11;

public class fhbm2VCopperPigLobotomyCutscene {

    private static final int TOTAL_FRAMES = 190;
    private static final int FRAME_INTERVAL_TICKS = 2; // 10 FPS = every 2 ticks (20 TPS)

    private static final Map<UUID, Integer> frameIndices = new HashMap<>();
    private static final Map<UUID, Integer> tickCounters = new HashMap<>();
    private static final Set<UUID> playingPlayers = new HashSet<>();

    public static void startPlayback(UUID playerId) {
        playingPlayers.add(playerId);
        frameIndices.put(playerId, 0);
        tickCounters.put(playerId, 0);
    }

    public static void stopPlayback(UUID playerId) {
        playingPlayers.remove(playerId);
        frameIndices.remove(playerId);
        tickCounters.remove(playerId);
    }

    public static boolean isPlaying(UUID playerId) {
        return playingPlayers.contains(playerId);
    }

    // called every tick, this is where we advance the frames
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        UUID playerId = mc.player.getUniqueID();
        if (!isPlaying(playerId)) return;

        int ticks = tickCounters.getOrDefault(playerId, 0) + 1;
        if (ticks >= FRAME_INTERVAL_TICKS) {
            tickCounters.put(playerId, 0);

            int currentFrame = frameIndices.getOrDefault(playerId, 0) + 1;
            if (currentFrame >= TOTAL_FRAMES) {
                stopPlayback(playerId);
            } else {
                frameIndices.put(playerId, currentFrame);
            }
        } else {
            tickCounters.put(playerId, ticks);
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        UUID playerId = mc.player.getUniqueID();
        if (!isPlaying(playerId)) return;

        int frame = frameIndices.getOrDefault(playerId, 0);

        String frameName = String.format("textures/gui/lobotomy_cutscene/lobotomy%03d.png", frame);
        ResourceLocation texture = new ResourceLocation("hbm", frameName);
        mc.getTextureManager().bindTexture(texture);

        ScaledResolution res = new ScaledResolution(mc);
        int screenWidth = res.getScaledWidth();
        int screenHeight = res.getScaledHeight();

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tess = Tessellator.getInstance();
        tess.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tess.getBuffer().pos(0, screenHeight, -90).tex(0.0D, 1.0D).endVertex();
        tess.getBuffer().pos(screenWidth, screenHeight, -90).tex(1.0D, 1.0D).endVertex();
        tess.getBuffer().pos(screenWidth, 0, -90).tex(1.0D, 0.0D).endVertex();
        tess.getBuffer().pos(0, 0, -90).tex(0.0D, 0.0D).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof net.minecraft.entity.player.EntityPlayer)) return;

        net.minecraft.entity.player.EntityPlayer player = (net.minecraft.entity.player.EntityPlayer) event.getEntityLiving();
        UUID playerId = player.getUniqueID();

        if (isPlaying(playerId)) {
            stopPlayback(playerId);
            // System.out.println("[fhbm2CopperPigLobotomyCutscene] Cutscene stopped due to player death.");
        }
    }
}
