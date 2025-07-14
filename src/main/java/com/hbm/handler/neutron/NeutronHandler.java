package com.hbm.handler.neutron;

import com.hbm.tileentity.machine.rbmk.RBMKDials;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
public class NeutronHandler {

    private static int ticks = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase != TickEvent.Phase.START)
            return;

        // Freshen the node cache every `cacheTime` ticks to prevent huge RAM usage from idle nodes.
        int cacheTime = 20;
        boolean cacheClear = ticks >= cacheTime;
        if(cacheClear) ticks = 0;
        ticks++;

        // Remove `StreamWorld` objects if they have no streams.
        NeutronNodeWorld.removeEmptyWorlds();

        for(Map.Entry<World, NeutronNodeWorld.StreamWorld> world : NeutronNodeWorld.streamWorlds.entrySet()) {

            // Gamerule caching because this apparently is kinda slow?
            // meh, good enough
            /*
            RBMKNeutronHandler.reflectorEfficiency = RBMKDials.getReflectorEfficiency(world.getKey());
            RBMKNeutronHandler.absorberEfficiency = RBMKDials.getAbsorberEfficiency(world.getKey());
            RBMKNeutronHandler.moderatorEfficiency = RBMKDials.getModeratorEfficiency(world.getKey());

            // I hate this.
            // this broke everything because it was ONE OFF
            // IT'S NOT THE TOTAL HEIGHT IT'S THE AMOUNT OF BLOCKS ABOVE AAAAAAAAAAAAA
            RBMKNeutronHandler.columnHeight = RBMKDials.getColumnHeight(world.getKey()) + 1;
            RBMKNeutronHandler.fluxRange = RBMKDials.getFluxRange(world.getKey());*/
            // Th3_Sl1ze: I am NOT touching rbmk shit for now, I'm sleepy enough to fuck this all up

            world.getValue().runStreamInteractions(world.getKey());
            world.getValue().removeAllStreams();

            if(cacheClear) world.getValue().cleanNodes();
        }
    }
}
