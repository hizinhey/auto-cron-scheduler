package org.sol.hizinhey.example;

import org.sol.hizinhey.annotation.CronComponent;
import org.sol.hizinhey.annotation.CronJob;

@CronComponent("data-processor")
public class MyTask2 {

    @CronJob("*/15 * * * * *") // Every 15 seconds (changed from daily for demo)
    public void processData() {
        System.out.println("Processing data: " + java.time.LocalDateTime.now());
    }

    @CronJob("*/20 * * * * *") // Every 20 seconds (changed from 15 minutes for demo)
    public void syncData() {
        System.out.println("Syncing data: " + java.time.LocalDateTime.now());
    }
}