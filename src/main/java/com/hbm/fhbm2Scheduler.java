package com.hbm;

import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

public class fhbm2Scheduler {

    private static class ScheduledTask {
        int delayTicks;
        Consumer<TickEvent.ServerTickEvent> action;

        ScheduledTask(int delayTicks, Consumer<TickEvent.ServerTickEvent> action) {
            this.delayTicks = delayTicks;
            this.action = action;
        }
    }

    private static final LinkedList<ScheduledTask> tasks = new LinkedList<>();

    public static void schedule(int delayTicks, Consumer<TickEvent.ServerTickEvent> action) {
        tasks.add(new ScheduledTask(delayTicks, action));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;

        Iterator<ScheduledTask> iter = tasks.iterator();
        while (iter.hasNext()) {
            ScheduledTask task = iter.next();
            task.delayTicks--;
            if (task.delayTicks <= 0) {
                task.action.accept(event);
                iter.remove();
            }
        }
    }

}
