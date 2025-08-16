package com.aembr.guesstheutils.utils;

import com.aembr.guesstheutils.GuessTheUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Scheduler {
    private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();

    static {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
            while (iterator.hasNext()) {
                ScheduledTask scheduledTask = iterator.next();
                scheduledTask.remainingTicks--;

                if (scheduledTask.remainingTicks <= 0) {
                    try {
                        scheduledTask.task.run();
                    } catch (Exception e) {
                        GuessTheUtils.LOGGER.error("Error executing scheduled task: {}", e.getMessage());
                    } finally {
                        iterator.remove();
                    }
                }
            }
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    public static ScheduledTask schedule(int delayTicks, Runnable task) {
        Objects.requireNonNull(task, "Scheduled task cannot be null");

        if (delayTicks < 0) {
            throw new IllegalArgumentException("Delay ticks must be non-negative");
        }

        ScheduledTask scheduledTask = new ScheduledTask(delayTicks, task);
        scheduledTasks.add(scheduledTask);
        return scheduledTask;
    }

    public static boolean cancel(ScheduledTask task) {
        return scheduledTasks.remove(task);
    }

    public static class ScheduledTask {
        private int remainingTicks;
        private final Runnable task;

        private ScheduledTask(int delayTicks, Runnable task) {
            this.remainingTicks = delayTicks;
            this.task = task;
        }
    }
}