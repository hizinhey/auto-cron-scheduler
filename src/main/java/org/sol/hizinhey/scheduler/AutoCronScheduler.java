package org.sol.hizinhey.scheduler;

import org.sol.hizinhey.annotation.CronComponent;
import org.sol.hizinhey.annotation.CronJob;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

public class AutoCronScheduler {
    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledFuture<?>> scheduledJobs;
    private final Map<String, Object> componentInstances;
    private boolean started = false;

    public AutoCronScheduler() {
        this.executor = Executors.newScheduledThreadPool(10);
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.componentInstances = new ConcurrentHashMap<>();
    }

    public AutoCronScheduler(int threadPoolSize) {
        this.executor = Executors.newScheduledThreadPool(threadPoolSize);
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.componentInstances = new ConcurrentHashMap<>();
    }

    /**
     * Start the scheduler and automatically scan for @CronComponent classes
     */
    public void start() {
        start(""); // Scan entire classpath
    }

    /**
     * Start the scheduler and scan specific package
     */
    public void start(String basePackage) {
        if (started) {
            System.out.println("Scheduler already started!");
            return;
        }

        System.out.println("Starting AutoCronScheduler...");
        scanAndRegisterComponents(basePackage);
        started = true;
        System.out.println("AutoCronScheduler started with " + scheduledJobs.size() + " jobs");
    }

    /**
     * Scan classpath for @CronComponent annotated classes
     */
    private void scanAndRegisterComponents(String basePackage) {
        try {
            Set<Class<?>> componentClasses = findCronComponents(basePackage);

            for (Class<?> clazz : componentClasses) {
                try {
                    // Create instance of the component
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String componentName = clazz.getSimpleName();

                    componentInstances.put(componentName, instance);
                    scanAndSchedule(instance);

                    System.out.println("Registered component: " + componentName);
                } catch (Exception e) {
                    System.err.println("Failed to instantiate component " + clazz.getSimpleName() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error during component scanning: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find all classes annotated with @CronComponent
     */
    private Set<Class<?>> findCronComponents(String basePackage) {
        Set<Class<?>> components = new HashSet<>();

        try {
            // Get all classes in classpath
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packagePath = basePackage.replace('.', '/');

            Enumeration<URL> resources = classLoader.getResources(packagePath);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File directory = new File(resource.getFile());
                    components.addAll(findClassesInDirectory(directory, basePackage));
                }
            }

            // Also scan default package if base package is empty
            if (basePackage.isEmpty()) {
                components.addAll(scanDefaultPackage());
            }

        } catch (Exception e) {
            System.err.println("Error finding components: " + e.getMessage());
        }

        return components;
    }

    /**
     * Scan default package and common packages
     */
    private Set<Class<?>> scanDefaultPackage() {
        Set<Class<?>> components = new HashSet<>();

        // Try to find classes by looking at the current execution context
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));

        for (String path : paths) {
            File file = new File(path);
            if (file.isDirectory()) {
                components.addAll(findClassesInDirectory(file, ""));
            }
        }

        return components;
    }

    /**
     * Find classes in directory recursively
     */
    private Set<Class<?>> findClassesInDirectory(File directory, String packageName) {
        Set<Class<?>> components = new HashSet<>();

        if (!directory.exists()) {
            return components;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return components;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName.isEmpty() ?
                        file.getName() : packageName + "." + file.getName();
                components.addAll(findClassesInDirectory(file, subPackage));
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().length() - 6);
                String fullClassName = packageName.isEmpty() ?
                        className : packageName + "." + className;

                try {
                    Class<?> clazz = Class.forName(fullClassName);
                    if (clazz.isAnnotationPresent(CronComponent.class)) {
                        components.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    // Ignore classes that can't be loaded
                }
            }
        }

        return components;
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

    /**
     * Simple cron parser - supports basic patterns
     */
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
            return 60; // every minute
        }

        // Parse hourly pattern
        if ("0".equals(minutes) && "*".equals(hours)) {
            return 3600; // every hour
        }

        // Parse specific minute intervals
        if (minutes.startsWith("*/")) {
            return Integer.parseInt(minutes.substring(2)) * 60;
        }

        // Default to every 30 seconds for demo
        return 30;
    }

    /**
     * Cancel a specific job
     */
    public boolean cancelJob(String jobName) {
        ScheduledFuture<?> future = scheduledJobs.get(jobName);
        if (future != null) {
            future.cancel(false);
            scheduledJobs.remove(jobName);
            return true;
        }
        return false;
    }

    /**
     * Get list of active jobs
     */
    public Set<String> getActiveJobs() {
        return new HashSet<>(scheduledJobs.keySet());
    }

    /**
     * Get component instances
     */
    public Map<String, Object> getComponentInstances() {
        return new HashMap<>(componentInstances);
    }

    /**
     * Shutdown the scheduler
     */
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
        started = false;
    }
}