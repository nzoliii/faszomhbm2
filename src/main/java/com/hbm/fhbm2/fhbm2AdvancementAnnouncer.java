package com.hbm.fhbm2;

import com.hbm.lib.RefStrings;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.*;

public class fhbm2AdvancementAnnouncer {

    private static final Map<UUID, Set<ResourceLocation>> knownCompletions = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        UUID uuid = player.getUniqueID();
        Set<ResourceLocation> completed = knownCompletions.computeIfAbsent(uuid, k -> new HashSet<>());

        Iterable<Advancement> allAdvancements = server.getAdvancementManager().getAdvancements();

        for (Advancement adv : allAdvancements) {
            if (!adv.getId().getNamespace().equals(RefStrings.MODID)) continue;

            if (!player.getAdvancements().getProgress(adv).isDone()) continue;

            ResourceLocation id = adv.getId();
            if (completed.contains(id)) continue;

            completed.add(id);

            DisplayInfo display = adv.getDisplay();
            if (display == null) continue;

            String rawTitle = display.getTitle().getFormattedText();

            TextFormatting color = getColorFromString(rawTitle);

            if (color == null) {
                switch (display.getFrame()) {
                    case CHALLENGE: color = TextFormatting.DARK_PURPLE; break;
                    case GOAL: color = TextFormatting.AQUA; break;
                    default: color = TextFormatting.GREEN; break;
                }
            }

            String title = stripLegacyColorCode(rawTitle);

            TextComponentTranslation prefix = new TextComponentTranslation("hbm.achievement.fhbm2_complete_msg", player.getDisplayName());

            TextComponentString advText = new TextComponentString(title);
            advText.getStyle().setColor(color);

            ITextComponent hoverText = new TextComponentString("");
            ITextComponent hoverTitle = display.getTitle().createCopy();
            ITextComponent hoverDescription = display.getDescription().createCopy();

            hoverTitle.getStyle().setColor(color);
            hoverDescription.getStyle().setColor(color);

            hoverText.appendSibling(hoverTitle);
            hoverText.appendText("\n");
            hoverText.appendSibling(hoverDescription);

            advText.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

            prefix.appendSibling(advText);

            server.getPlayerList().sendMessage(prefix);
        }
    }

    private static TextFormatting getColorFromString(String text) {
        if (text == null || text.length() < 2) return null;
        if (text.charAt(0) != '§') return null;

        char code = text.charAt(1);
        for (TextFormatting format : TextFormatting.values()) {
            if (!format.isColor()) continue;
            if (format.toString().length() < 2) continue;
            if (format.toString().charAt(1) == code) {
                return format;
            }
        }
        return null;
    }

    private static String stripLegacyColorCode(String text) {
        if (text == null) return "";
        if (text.startsWith("§") && text.length() > 2) {
            return text.substring(2);
        }
        return text;
    }
}