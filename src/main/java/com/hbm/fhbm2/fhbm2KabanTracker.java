package com.hbm.fhbm2;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class fhbm2KabanTracker {

    // I really should clean this class up. Will do, later.

    private static final Map<UUID, Boolean> bewitchedPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playersWhoAteFragment = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playersHavingVisions = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playersHavingFleshVisions = new ConcurrentHashMap<>();

    // --------- DIVIDER --------- //

    public static void setPlayerBewitched(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            bewitchedPlayers.put(id, true);
        } else {
            bewitchedPlayers.remove(id);
        }
    }

    public static boolean isPlayerBewitched(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return bewitchedPlayers.containsKey(id) && bewitchedPlayers.get(id);
    }

    // --------- DIVIDER --------- //

    public static void setPlayersWhoAteFragment(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            playersWhoAteFragment.put(id, true);
        } else {
            playersWhoAteFragment.remove(id);
        }
    }

    public static boolean hasPlayersAteFragment(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return playersWhoAteFragment.containsKey(id) && playersWhoAteFragment.get(id);
    }

    // --------- DIVIDER --------- //

    public static void setPlayersHavingVisions(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            playersHavingVisions.put(id, true);
        } else {
            playersHavingVisions.remove(id);
        }
    }

    public static boolean isPlayersHavingVisions(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return playersHavingVisions.containsKey(id) && playersHavingVisions.get(id);
    }

    // --------- DIVIDER --------- //

    public static void setPlayersHavingFleshVisions(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            playersHavingFleshVisions.put(id, true);
        } else {
            playersHavingFleshVisions.remove(id);
        }
    }

    public static boolean isPlayerHavingFleshVisions(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return playersHavingFleshVisions.containsKey(id) && playersHavingFleshVisions.get(id);
    }

    // --------- DIVIDER --------- //

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        UUID id = player.getUniqueID();

        bewitchedPlayers.remove(id);
        playersWhoAteFragment.remove(id);
        playersHavingVisions.remove(id);
        playersHavingFleshVisions.remove(id);
    }
}
