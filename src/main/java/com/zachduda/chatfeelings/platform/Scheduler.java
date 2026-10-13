package com.zachduda.chatfeelings.platform;

import com.zachduda.chatfeelings.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import space.arim.morepaperlib.MorePaperLib;
import space.arim.morepaperlib.scheduling.GracefulScheduling;
import space.arim.morepaperlib.scheduling.ScheduledTask;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Where ChatFeelings runs things. On Folia an entity may only be touched from the region thread that owns
 * it, so world/entity work goes through {@link #entity}; on Spigot/Paper that's simply the main thread.
 * <p>
 * Blocking work (player data storage) runs on ChatFeelings' own small worker pool rather than the server's
 * shared async pool, so it can be drained on shutdown instead of being cancelled half-done.
 */
public final class Scheduler {
    private final Plugin plugin;
    private final GracefulScheduling scheduling;
    private final boolean folia;
    private final ThreadPoolExecutor workers;

    public Scheduler(Plugin plugin, MorePaperLib morePaperLib) {
        this.plugin = plugin;
        this.scheduling = morePaperLib.scheduling();
        this.folia = scheduling.isUsingFolia();

        final AtomicInteger threadIds = new AtomicInteger();
        this.workers = new ThreadPoolExecutor(4, 4, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), runnable -> {
            Thread thread = new Thread(runnable, "ChatFeelings Worker #" + threadIds.incrementAndGet());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, err) -> Log.severe("Error in " + t.getName(), err));
            return thread;
        });
        this.workers.allowCoreThreadTimeOut(true);
    }

    public boolean isFolia() {
        return folia;
    }

    /** Runs blocking work (storage, lookups) off the server threads. */
    public void async(Runnable task) {
        try {
            workers.execute(() -> {
                try {
                    task.run();
                } catch (Throwable err) {
                    Log.severe("An error occurred in a ChatFeelings background task", err);
                }
            });
        } catch (RejectedExecutionException shuttingDown) {
            Log.debug("Dropped a background task because ChatFeelings is shutting down.");
        }
    }

    /** Runs on the global region (Folia) or main thread; immediately if already on the Spigot/Paper main thread. */
    public void global(Runnable task) {
        if (!folia && Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        guard(() -> scheduling.globalRegionalScheduler().run(task));
    }

    /**
     * Runs on the global region / main thread on the next tick, never immediately. Called during onEnable it
     * runs once the server has finished starting, after every plugin (and, on Spigot, vanilla) has registered
     * its commands.
     */
    public void globalNextTick(Runnable task) {
        guard(() -> scheduling.globalRegionalScheduler().run(task));
    }

    /**
     * Runs on the thread that owns {@code entity}, immediately if already there (Spigot/Paper main thread).
     * If the entity is gone (ie: the player logged out) {@code retired} runs instead, when given.
     */
    public void entity(Entity entity, Runnable task, Runnable retired) {
        if (!folia && Bukkit.isPrimaryThread()) {
            task.run();
            return;
        }
        guard(() -> {
            ScheduledTask scheduled = scheduling.entitySpecificScheduler(entity).run(task, retired);
            if (scheduled == null && folia && retired != null) {
                retired.run(); // Folia returns null when the entity has already been removed
            }
        });
    }

    public void entity(Entity entity, Runnable task) {
        entity(entity, task, null);
    }

    /** Runs on {@code entity}'s thread after {@code ticks} ticks. Skipped if the entity is gone by then. */
    public void entityLater(Entity entity, Runnable task, long ticks) {
        guard(() -> scheduling.entitySpecificScheduler(entity).runDelayed(task, null, Math.max(1, ticks)));
    }

    /** Runs on the thread a command sender belongs to: their own thread for players, the global one otherwise. */
    public void forSender(CommandSender sender, Runnable task) {
        if (sender instanceof Player player) {
            entity(player, task);
        } else {
            global(task);
        }
    }

    /** Repeating task on the server's async pool, for slow network checks that shouldn't occupy workers. */
    public ScheduledTask asyncRepeating(Runnable task, Duration initialDelay, Duration period) {
        return scheduling.asyncScheduler().runAtFixedRate(task, initialDelay, period);
    }

    /** One-off task on the server's async pool. */
    public void asyncServer(Runnable task) {
        guard(() -> scheduling.asyncScheduler().run(task));
    }

    /**
     * Stops accepting work and waits for queued storage tasks to finish, so the last stats and last-on times
     * are written before storage closes.
     */
    public void shutdown(long timeoutSeconds) {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                Log.warn("Some ChatFeelings background tasks didn't finish within " + timeoutSeconds + "s of shutdown.");
                workers.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            workers.shutdownNow();
        }
        try {
            scheduling.cancelGlobalTasks();
        } catch (RuntimeException err) {
            Log.debug("Unable to cancel scheduled tasks: " + err);
        }
    }

    /** Scheduling throws once the plugin is disabled; work arriving that late is simply dropped. */
    private void guard(Runnable schedule) {
        if (!plugin.isEnabled()) {
            Log.debug("Skipped scheduling a task because ChatFeelings is disabled.");
            return;
        }
        try {
            schedule.run();
        } catch (IllegalStateException | UnsupportedOperationException err) {
            Log.debug("Unable to schedule a task: " + err);
        } catch (RuntimeException err) {
            // Bukkit throws IllegalPluginAccessException (a RuntimeException) when disabling.
            if (plugin.isEnabled()) {
                throw err;
            }
        }
    }
}
