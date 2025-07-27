package org.sol.hizinhey.example;

import org.sol.hizinhey.annotation.CronComponent;
import org.sol.hizinhey.annotation.CronJob;

@CronComponent()
public class MyTask {
    @CronJob("*/10 * * * * *") // Every 10 seconds
    public void taskEvery10Seconds() {
        System.out.println("Automatic task executed every 10 seconds: " + java.time.LocalDateTime.now());
    }

    @CronJob(value = "0 */1 * * * *", name = "auto-report") // Every minute
    public void generateReport() {
        System.out.println("Automatic report generation: " + java.time.LocalDateTime.now());
    }

    @CronJob(value = "*/5 * * * * *", startImmediately = true) // Every 5 seconds, start immediately
    public void healthCheck() {
        System.out.println("Automatic health check: " + java.time.LocalDateTime.now());
    }
}
