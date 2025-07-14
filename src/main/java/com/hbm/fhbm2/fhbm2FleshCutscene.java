package com.hbm.fhbm2;

import java.io.IOException;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class fhbm2FleshCutscene {

    private static final int TOTAL_FRAMES = 220;
    private static final int FRAME_INTERVAL_TICKS = 2;
    private static final String BASE_PATH = "textures/gui/flesh_cutscene/flesh";

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

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.player == null || mc.isGamePaused()) return;

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
        ResourceLocation texture = getFrameResource(frame);
        if (texture == null) return;

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

    private static ResourceLocation getFrameResource(int frameNumber) {
        Minecraft mc = Minecraft.getMinecraft();
        String padded = String.format("%03d", frameNumber);

        ResourceLocation png = new ResourceLocation("hbm", BASE_PATH + padded + ".png");
        try {
            mc.getResourceManager().getResource(png);
            return png;
        } catch (IOException ignored) {}

        ResourceLocation jpg = new ResourceLocation("hbm", BASE_PATH + padded + ".jpg");
        try {
            mc.getResourceManager().getResource(jpg);
            return jpg;
        } catch (IOException ignored) {}

        return null;
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof net.minecraft.entity.player.EntityPlayer)) return;

        UUID playerId = event.getEntityLiving().getUniqueID();
        if (isPlaying(playerId)) {
            stopPlayback(playerId);
        }
    }
}
