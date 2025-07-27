package org.sol.hizinhey.scheduler;

import org.sol.hizinhey.annotation.CronJob;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class CronScheduler {
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final List<Object> registeredObjects;

    public CronScheduler() {
        this.executor = Executors.newScheduledThreadPool(10);
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.registeredObjects = new ArrayList<>();
    }

    public CronScheduler(int threadPoolSize) {
        this.executor = Executors.newScheduledThreadPool(threadPoolSize);
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.registeredObjects = new ArrayList<>();
    }

    /**
     * Register an object and scan for @CronJob annotated methods
     */
    public void register(Object obj) {
        registeredObjects.add(obj);
        scanAndSchedule(obj);
    }

    /**
     * Scan object for @CronJob annotations and schedule them
     */
    private void scanAndSchedule(Object obj) {
        Class<?> clazz = obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(CronJob.class)) {
                CronJob cronJob = method.getAnnotation(CronJob.class);
                scheduleMethod(obj, method, cronJob);
            }
        }
    }

    /**
     * Schedule a method with cron expression
     */
    private void scheduleMethod(Object obj, Method method, CronJob cronJob) {
        String jobName = cronJob.name().isEmpty() ?
                obj.getClass().getSimpleName() + "." + method.getName() : cronJob.name();

        // Make method accessible if private
        method.setAccessible(true);

        // Create runnable task
        Runnable task = () -> {
            try {
                System.out.println("[" + LocalDateTime.now() + "] Executing job: " + jobName);
                method.invoke(obj);
            } catch (Exception e) {
                System.err.println("Error executing job " + jobName + ": " + e.getMessage());
                e.printStackTrace();
            }
        };

        // Parse cron and schedule
        try {
            long initialDelay = calculateInitialDelay(cronJob.value());
            long period = calculatePeriod(cronJob.value());

            if (cronJob.startImmediately()) {
                initialDelay = 0;
            }

            ScheduledFuture<?> future = executor.scheduleAtFixedRate(
                    task, initialDelay, period, TimeUnit.SECONDS
            );

            scheduledJobs.put(jobName, future);
            System.out.println("Scheduled job: " + jobName + " with cron: " + cronJob.value());

        } catch (Exception e) {
            System.err.println("Failed to schedule job " + jobName + ": " + e.getMessage());
        }
    }

    private long calculateInitialDelay(String cronExpression) {
        return 0;
    }

    private long calculatePeriod(String cronExpression) {
        String[] parts = cronExpression.split("\\s+");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        String seconds = parts[0];
        String minutes = parts[1];
        String hours = parts[2];

        // Parse seconds pattern
        if (seconds.startsWith("*/")) {
            return Integer.parseInt(seconds.substring(2));
        }

        // Parse minutes pattern
        if ("*".equals(minutes) && "*".equals(hours)) {
            return 60;
        }

        if ("0".equals(minutes) && "*".equals(hours)) {
            return 3600;
        }

        if (minutes.startsWith("*/")) {
            return Integer.parseInt(minutes.substring(2)) * 60;
        }

        return 30;
    }

    public boolean cancelJob(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.get(jobName);
        if (future != null) {
            future.cancel(false);
            scheduledJobs.remove(jobName);
            return true;
        }
        return false;
    }

    public Set<String> getActiveJobs() {
        return new HashSet<>(scheduledJobs.keySet());
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
