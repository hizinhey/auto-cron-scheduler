package org.sol.hizinhey.example;


import org.sol.hizinhey.scheduler.AutoCronScheduler;

public class MyScheduledTasks {
    // Manual registration demo
    public static void main(String[] args) {
        System.out.println("=== Starting Manual Cron Scheduler Demo ===");

        AutoCronScheduler scheduler = new AutoCronScheduler();

        // It will automatically find and register all @CronComponent classes
        scheduler.start("org.sol.hizinhey.example"); // Scan specific package

        System.out.println("Active jobs: " + scheduler.getActiveJobs());
        System.out.println("Registered components: " + scheduler.getComponentInstances().keySet());

        try {
            System.out.println("Running for 20 seconds...");
            Thread.sleep(20000); // Run for 20 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        scheduler.shutdown();
        System.out.println("Manual scheduler stopped.");
    }
}