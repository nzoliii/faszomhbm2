package com.hbm.fhbm2;

import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class fhbm2CutsceneItemTracker {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        // System.out.println("Tick for player: " + player.getName());

        checkHoldingKeyAndTriggerCutscene(player);
        checkHoldingFleshTrigger(player);
    }

    private void checkHoldingKeyAndTriggerCutscene(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        boolean holdingItem = (mainHand != null && !mainHand.isEmpty() && mainHand.getItem() == ModItems.fhbm2_key_kaban)
                || (offHand != null && !offHand.isEmpty() && offHand.getItem() == ModItems.fhbm2_key_kaban);

        if (holdingItem) {
            boolean hasVisions = fhbm2KabanTracker.isPlayersHavingVisions(player);

            if (!hasVisions) {

                fhbm2KabanTracker.setPlayersHavingVisions(player, true);
                playerHasVisionsFromTouchingTheKey(player);

            }
        }
    }

    private void checkHoldingFleshTrigger(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        boolean holdingItem = (mainHand != null && !mainHand.isEmpty() && mainHand.getItem() == ModItems.chlorine_pinwheel)
                || (offHand != null && !offHand.isEmpty() && offHand.getItem() == ModItems.chlorine_pinwheel);

        if (holdingItem) {
            boolean hasFleshVisions = fhbm2KabanTracker.isPlayerHavingFleshVisions(player);

            if (!hasFleshVisions) {

                fhbm2KabanTracker.setPlayersHavingFleshVisions(player, true);
                playerSeesTheFlesh(player);

            }
        }
    }

    public void playerHasVisionsFromTouchingTheKey(EntityPlayer player) {
        AdvancementManager.grantAchievement((player), AdvancementManager.fhbm2_copper_pig_key);

        fhbm2Scheduler.schedule(0, (event) -> {

            player.playSound(HBMSoundHandler.fhbm2_heartbeat, 1.0F, 1.0F);
            fhbm2KabanPTSDCutscene.startPlayback(player.getUniqueID());
        });

        fhbm2Scheduler.schedule(5 * 20, (event) -> {
            fhbm2KabanTracker.setPlayersHavingVisions(player, false);
        });
    }

    public void playerSeesTheFlesh(EntityPlayer player) {
        fhbm2Scheduler.schedule(0, (event) -> {

            player.playSound(HBMSoundHandler.fhbm2_flesh, 1.0F, 1.0F);
            fhbm2FleshCutscene.startPlayback(player.getUniqueID());
        });

        fhbm2Scheduler.schedule(23 * 20, (event) -> {
            fhbm2KabanTracker.setPlayersHavingFleshVisions(player, false);
        });
    }
}
