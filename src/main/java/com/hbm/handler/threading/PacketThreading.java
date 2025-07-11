package com.hbm.handler.threading;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hbm.config.GeneralConfig;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.threading.ThreadedPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class PacketThreading {

    public static final String threadPrefix = "NTM-Packet-Thread-";
    public static final ThreadFactory packetThreadFactory = new ThreadFactoryBuilder().setNameFormat(threadPrefix + "%d").build();
    public static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(1, packetThreadFactory);

    /**
     * Futures returned by {@link #threadPool}. The list is cleared by {@link #waitUntilThreadFinished()} once all
     * packets have either completed, errored, or timed out.
     */
    public static final List<Future<?>> futureList = new ArrayList<>();

    /**
     * Global lock guarding the FML channel state for outbound packets.
     * <p>
     * FML's networking uses thread-local attributes on the channel to determine a packet's destination (e.g., a specific
     * player, a dimension). Modifying these attributes from multiple threads concurrently is not safe. This lock ensures
     * that only one thread can set these attributes and write to the channel at a time.
     * <p>
     * The lock is held for the shortest possible duration. Expensive operations like packet serialization happen
     * on worker threads *before* the lock is acquired.
     */
    public static final ReentrantLock lock = new ReentrantLock();

    /** Total packets submitted since the last flush. */
    public static int totalCnt = 0;
    /** Total nanoseconds the main thread waited for worker completion. */
    public static long nanoTimeWaited = 0;

    public static int clearCnt = 0;
    public static boolean hasTriggered = false;

    /**
     * Sets up thread pool settings during mod initialization.
     */
    public static void init() {
        threadPool.setKeepAliveTime(50, TimeUnit.MILLISECONDS);
        if (GeneralConfig.enablePacketThreading) {
            int coreCount = GeneralConfig.packetThreadingCoreCount;
            int maxCount = GeneralConfig.packetThreadingMaxCount;

            if (coreCount <= 0 || maxCount <= 0) {
                MainRegistry.logger.error("packetThreadingCoreCount ({}) or packetThreadingMaxCount ({}) is <= 0. Defaulting to a single-threaded pool.", coreCount, maxCount);
                threadPool.setCorePoolSize(1);
                threadPool.setMaximumPoolSize(1);
            } else if (maxCount < coreCount) {
                MainRegistry.logger.warn("packetThreadingMaxCount ({}) cannot be less than packetThreadingCoreCount ({}). Setting max count to core count.", maxCount, coreCount);
                threadPool.setCorePoolSize(coreCount);
                threadPool.setMaximumPoolSize(coreCount);
            } else {
                threadPool.setCorePoolSize(coreCount);
                threadPool.setMaximumPoolSize(maxCount);
            }
            threadPool.allowCoreThreadTimeOut(false);
        } else {
            threadPool.allowCoreThreadTimeOut(true);
            try {
                lock.lock();
                for (Runnable task : threadPool.getQueue()) {
                    task.run(); // Drain outstanding tasks synchronously.
                }
                clearThreadPoolTasks();
            } finally {
                lock.unlock();
            }
        }
    }

    private static void addTask(Runnable task) {
        if (isTriggered()) {
            task.run();
        } else if (GeneralConfig.enablePacketThreading) {
            futureList.add(threadPool.submit(task));
        } else {
            task.run();
        }
    }

    /**
     * A helper method to create a runnable task for a {@link ThreadedPacket}.
     * This moves the expensive serialization to the worker thread and minimizes lock contention.
     *
     * @param packet     The packet to serialize and send.
     * @param sendAction The lambda performing the network write (e.g., {@code () -> wrapper.sendTo(...)}).
     * @return A {@link Runnable} to be submitted to the thread pool.
     */
    private static Runnable createTask(@NotNull ThreadedPacket packet, @NotNull Runnable sendAction) {
        return () -> {
            packet.getCompiledBuffer();
            sendAction.run();
        };
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToServer(IMessage)}. */
    public static void createSendToServerThreadedPacket(@NotNull ThreadedPacket message) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToServer(message)));
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToDimension(IMessage, int)}. */
    public static void createSendToDimensionThreadedPacket(@NotNull ThreadedPacket message, int dimensionId) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToDimension(message, dimensionId)));
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToAllAround(IMessage, TargetPoint)}. */
    public static void createAllAroundThreadedPacket(@NotNull ThreadedPacket message, @NotNull TargetPoint target) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToAllAround(message, target)));
    }

    /**
     * Mirrors {@link com.hbm.main.NetworkHandler#sendToAllAround(ByteBuf, TargetPoint)}.
     * This method is safer for concurrency as it deals with a stateless buffer.
     */
    public static void createAllAroundThreadedPacket(@NotNull ByteBuf buffer, @NotNull TargetPoint target) {
        totalCnt++;

        // Retain a reference for thread-safe use; will be released inside the task.
        final ByteBuf retained = buffer.retainedDuplicate();

        Runnable task = () -> {
            try {
                PacketDispatcher.wrapper.sendToAllAround(retained, target);
            } finally {
                retained.release();
            }
        };

        addTask(task);
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToAllTracking(IMessage, TargetPoint)}. */
    public static void createSendToAllTrackingThreadedPacket(@NotNull ThreadedPacket message, @NotNull TargetPoint point) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToAllTracking(message, point)));
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToAllTracking(IMessage, Entity)}. */
    public static void createSendToAllTrackingThreadedPacket(@NotNull ThreadedPacket message, @NotNull Entity entity) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToAllTracking(message, entity)));
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendTo(IMessage, EntityPlayerMP)}. */
    public static void createSendToThreadedPacket(@NotNull ThreadedPacket message, @NotNull EntityPlayerMP player) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendTo(message, player)));
    }

    /** Mirrors {@link com.hbm.main.NetworkHandler#sendToAll(IMessage)}. */
    public static void createSendToAllThreadedPacket(@NotNull ThreadedPacket message) {
        totalCnt++;
        addTask(createTask(message, () -> PacketDispatcher.wrapper.sendToAll(message)));
    }

    /**
     * Blocks the caller until every previously scheduled packet task finishes execution,
     * but gives up if the total wait time exceeds 50 milliseconds to prevent stalling the main thread.
     */
    public static void waitUntilThreadFinished() {
        if (futureList.isEmpty() || !GeneralConfig.enablePacketThreading || isTriggered()) {
            futureList.clear();
            totalCnt = 0;
            return;
        }

        long startTime = System.nanoTime();
        try {
            if (GeneralConfig.enablePacketThreading && !hasTriggered) {
                long deadline = startTime + TimeUnit.MILLISECONDS.toNanos(50);
                for (Future<?> future : futureList) {
                    long timeoutLeft = deadline - System.nanoTime();
                    if (timeoutLeft <= 0) {
                        throw new TimeoutException("Packet processing deadline exceeded.");
                    }
                    future.get(timeoutLeft, TimeUnit.NANOSECONDS);
                }
            }
        } catch (ExecutionException e) {
            MainRegistry.logger.error("A packet processing task threw an exception.", e.getCause());
        } catch (TimeoutException e) {
            if (!GeneralConfig.packetThreadingErrorBypass && !hasTriggered) {
                MainRegistry.logger.warn("A packet task timed out or the total wait time (>50ms) was exceeded. Discarding {} remaining packets out of {} total to prevent server stall.", threadPool.getQueue().size(), totalCnt);
            }
            clearThreadPoolTasks();
        } catch (InterruptedException e) {
            MainRegistry.logger.warn("Packet waiting thread was interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            nanoTimeWaited = System.nanoTime() - startTime;
            futureList.clear();
            if (!threadPool.getQueue().isEmpty()) {
                if (!GeneralConfig.packetThreadingErrorBypass && !hasTriggered) {
                    MainRegistry.logger.warn("Residual packets detected in queue after processing. Discarding {} packets.", threadPool.getQueue().size());
                }
                clearThreadPoolTasks();
            }
            totalCnt = 0;
        }
    }

    /**
     * Forcibly removes every queued task without executing them.
     */
    public static void clearThreadPoolTasks() {
        if (threadPool.getQueue().isEmpty()) {
            clearCnt = 0;
            return;
        }

        threadPool.getQueue().clear();

        if (!GeneralConfig.packetThreadingErrorBypass && !hasTriggered) {
            MainRegistry.logger.warn("Packet work queue cleared forcefully (clear count: {}).", clearCnt);
        }

        clearCnt++;

        if (clearCnt > 5 && !isTriggered()) {
            MainRegistry.logger.error(
                    "Something has gone wrong and the packet pool has cleared 5 times (or more) in a row. "
                            + "This can indicate that the thread has been killed, suspended, or is otherwise non-functioning. "
                            + "This message will only be logged once, further packet operations will continue on the main thread. "
                            + "If this message is a common occurrence and is *completely expected*, then it can be bypassed permanently by setting "
                            + "the \"0.04_packetThreadingErrorBypass\" config option to true. This can lead to adverse effects, so do this at your own risk. "
                            + "Running \"/ntmpacket resetState\" resets this trigger as a temporary fix."
            );
            hasTriggered = true;
        }
    }

    /**
     * Indicates whether packet threading is presently bypassed due to repeated failures.
     */
    public static boolean isTriggered() {
        return hasTriggered && !GeneralConfig.packetThreadingErrorBypass;
    }
}