package com.hbm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class fhbm2KabanTracker {

    private static final Map<UUID, Boolean> bewitchedPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> playersWhoAteFragment = new ConcurrentHashMap<>();

    // Mark a player as bewitched or not
    public static void setPlayerBewitched(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            bewitchedPlayers.put(id, true);
        } else {
            bewitchedPlayers.remove(id);
        }
    }

    // Check if a player is bewitched
    public static boolean isPlayerBewitched(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return bewitchedPlayers.containsKey(id) && bewitchedPlayers.get(id);
    }

    // Mark a player as having eaten the fragment or not
    public static void setPlayersWhoAteFragment(EntityPlayer player, boolean value) {
        UUID id = player.getUniqueID();
        if (value) {
            playersWhoAteFragment.put(id, true);
        } else {
            playersWhoAteFragment.remove(id);
        }
    }

    // Check if a player has eaten the fragment
    public static boolean hasPlayersAteFragment(EntityPlayer player) {
        UUID id = player.getUniqueID();
        return playersWhoAteFragment.containsKey(id) && playersWhoAteFragment.get(id);
    }

    // Automatically clear players from the lists when they die
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.getEntity();
        UUID id = player.getUniqueID();

        bewitchedPlayers.remove(id);
        playersWhoAteFragment.remove(id);
    }
}
