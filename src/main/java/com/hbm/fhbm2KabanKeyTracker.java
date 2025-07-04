package com.hbm;

import com.hbm.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class fhbm2KabanKeyTracker {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        EntityPlayer player = event.player;
        // System.out.println("Tick for player: " + player.getName());

        checkHoldingKeyAndTriggerCutscene(player);
    }

    private void checkHoldingKeyAndTriggerCutscene(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        ItemStack offHand = player.getHeldItemOffhand();

        boolean holdingItem = (mainHand != null && !mainHand.isEmpty() && mainHand.getItem() == ModItems.fhbm2_key_kaban)
                || (offHand != null && !offHand.isEmpty() && offHand.getItem() == ModItems.fhbm2_key_kaban);

        if (holdingItem) {
            boolean hasVisions = fhbm2KabanTracker.isPlayersHavingVisions(player);

            if (!hasVisions) {

                playerHasVisionsFromTouchingTheKey(player);

            }
        }
    }

    public void playerHasVisionsFromTouchingTheKey(EntityPlayer player) {
        fhbm2Scheduler.schedule(0, (event) -> {
            fhbm2KabanPTSDCutscene.startPlayback(player.getUniqueID());
            fhbm2KabanTracker.setPlayersHavingVisions(player, true);

            // System.out.println("PTSD START, PLAYER HAS VISION");
        });

        fhbm2Scheduler.schedule(5 * 20, (event) -> {
            fhbm2KabanTracker.setPlayersHavingVisions(player, false);

            // System.out.println("PTSD STOP, PLAYER NO VISION");
        });
    }
}
