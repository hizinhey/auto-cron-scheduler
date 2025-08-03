# 🕒 AutoCron Scheduler

A lightweight, annotation-based cron scheduling library for Java that automatically discovers and schedules tasks using reflection and component scanning.

[![Java](https://img.shields.io/badge/Java-8%2B-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)]()

## 🚀 Features

- ✅ **Zero Configuration** - Just add annotations and start!
- ✅ **Automatic Component Discovery** - Scans classpath for `@CronComponent` classes
- ✅ **Cron Expression Support** - Standard 6-field cron expressions
- ✅ **Thread Pool Management** - Configurable concurrent execution
- ✅ **Job Management** - View, cancel, and monitor active jobs
- ✅ **Manual Registration** - Traditional registration still supported
- ✅ **No External Dependencies** - Pure Java implementation

## 📦 Installation

### Maven
```xml
<dependency>
    <groupId>com.sol.hizinhe</groupId>
    <artifactId>auto-cron-scheduler</artifactId>
    <version>0.0.0</version>
</dependency>
```

### Gradle
```gradle
implementation 'com.sol.hizinhey:auto-cron-scheduler:0.0.0'
```

** Note: I am not push it to maven repository.

### Manual
Download the JAR file and add it to your classpath.

## 🎯 Quick Start

### 1. Create Your Task Classes

```java
package com.yourapp.tasks;

import com.cronlib.annotation.CronComponent;
import com.cronlib.annotation.CronJob;

@CronComponent
public class MyAutomaticTasks {
    
    @CronJob("*/10 * * * * *") // Every 10 seconds
    public void taskEvery10Seconds() {
        System.out.println("Task executed: " + java.time.LocalDateTime.now());
    }
    
    @CronJob(value = "0 */1 * * * *", name = "report-generator") // Every minute
    public void generateReport() {
        System.out.println("Generating report: " + java.time.LocalDateTime.now());
    }
    
    @CronJob(value = "*/5 * * * * *", startImmediately = true) // Every 5 seconds, start now
    public void healthCheck() {
        System.out.println("Health check: " + java.time.LocalDateTime.now());
    }
}

@CronComponent("data-processor")
public class DataProcessor {
    
    @CronJob("*/15 * * * * *") // Every 15 seconds
    public void processData() {
        System.out.println("Processing data: " + java.time.LocalDateTime.now());
    }
    
    @CronJob("*/20 * * * * *") // Every 20 seconds
    public void syncData() {
        System.out.println("Syncing data: " + java.time.LocalDateTime.now());
    }
}
```

### 2. Start the Scheduler

```java
package com.yourapp;

import com.cronlib.scheduler.AutoCronScheduler;

public class Application {
    public static void main(String[] args) {
        // Create and start the scheduler
        AutoCronScheduler scheduler = new AutoCronScheduler();
        
        // Automatically discover and register all @CronComponent classes
        scheduler.start("com.yourapp.tasks"); // Scan specific package
        // Or use scheduler.start(); to scan entire classpath
        
        System.out.println("Active jobs: " + scheduler.getActiveJobs());
        System.out.println("Registered components: " + scheduler.getComponentInstances().keySet());
        
        // Keep running...
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdown));
    }
}
```

### 3. Run Your Application

```bash
java com.yourapp.Application
```

**Output:**
```
Starting AutoCronScheduler...
Registered component: MyAutomaticTasks
Registered component: DataProcessor
Scheduled job: MyAutomaticTasks.taskEvery10Seconds with cron: */10 * * * * *
Scheduled job: report-generator with cron: 0 */1 * * * *
Scheduled job: MyAutomaticTasks.healthCheck with cron: */5 * * * * *
Scheduled job: DataProcessor.processData with cron: */15 * * * * *
Scheduled job: DataProcessor.syncData with cron: */20 * * * * *
AutoCronScheduler started with 5 jobs
Active jobs: [MyAutomaticTasks.taskEvery10Seconds, report-generator, MyAutomaticTasks.healthCheck, DataProcessor.processData, DataProcessor.syncData]
Registered components: [MyAutomaticTasks, DataProcessor]

[2024-01-15T10:30:00] Executing job: MyAutomaticTasks.healthCheck
Health check: 2024-01-15T10:30:00
[2024-01-15T10:30:05] Executing job: MyAutomaticTasks.healthCheck
Health check: 2024-01-15T10:30:05
[2024-01-15T10:30:10] Executing job: MyAutomaticTasks.taskEvery10Seconds
Task executed: 2024-01-15T10:30:10
...
```

## 📚 Documentation

### Annotations

#### `@CronComponent`
Mark classes for automatic discovery and registration.

```java
@CronComponent // Default component name
@CronComponent("custom-name") // Custom component name
public class MyTasks { }
```

#### `@CronJob`
Mark methods for cron scheduling.

```java
@CronJob("0 0 12 * * *") // Daily at noon
@CronJob(value = "*/30 * * * * *", name = "custom-job") // Custom job name
@CronJob(value = "*/10 * * * * *", startImmediately = true) // Start immediately
public void myTask() { }
```

### Cron Expression Format

Format: `second minute hour day month dayOfWeek`

| Field | Values | Special Characters |
|-------|--------|--------------------|
| Second | 0-59 | `* / , -` |
| Minute | 0-59 | `* / , -` |
| Hour | 0-23 | `* / , -` |
| Day | 1-31 | `* / , -` |
| Month | 1-12 | `* / , -` |
| Day of Week | 0-7 (0,7=Sunday) | `* / , -` |

### Examples

| Expression | Description |
|------------|-------------|
| `*/10 * * * * *` | Every 10 seconds |
| `0 */5 * * * *` | Every 5 minutes |
| `0 0 */2 * * *` | Every 2 hours |
| `0 0 9 * * MON-FRI` | Weekdays at 9 AM |
| `0 30 10 * * *` | Daily at 10:30 AM |
| `0 0 0 1 * *` | First day of every month |

### Configuration Options

#### Thread Pool Size
```java
AutoCronScheduler scheduler = new AutoCronScheduler(20); // 20 threads
```

#### Package Scanning
```java
scheduler.start(); // Scan entire classpath
scheduler.start("com.myapp"); // Scan specific package
scheduler.start("com.myapp.tasks"); // Scan sub-package
```

### Job Management

```java
// Get active jobs
Set<String> activeJobs = scheduler.getActiveJobs();

// Cancel a specific job
boolean cancelled = scheduler.cancelJob("job-name");

// Get registered components
Map<String, Object> components = scheduler.getComponentInstances();

// Shutdown scheduler
scheduler.shutdown();
```

## 🔧 Manual Registration (Alternative)

If you prefer manual control over registration:

```java
import com.cronlib.scheduler.CronScheduler;

public class ManualExample {
    @CronJob("*/5 * * * * *")
    public void myTask() {
        System.out.println("Manual task executed");
    }
    
    public static void main(String[] args) {
        CronScheduler scheduler = new CronScheduler();
        scheduler.register(new ManualExample());
        
        // Keep running...
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        scheduler.shutdown();
    }
}
```

## 🏗️ Architecture

```
┌─────────────────────────────────────────────┐
│              AutoCronScheduler              │
├─────────────────────────────────────────────┤
│ 1. Component Scanning                       │
│ 2. Class Loading & Instantiation           │
│ 3. Method Discovery                         │
│ 4. Cron Expression Parsing                 │
│ 5. Task Scheduling                          │
└─────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────┐
│          ScheduledExecutorService           │
├─────────────────────────────────────────────┤
│ • Thread Pool Management                    │
│ • Concurrent Task Execution                 │
│ • Job Lifecycle Management                  │
└─────────────────────────────────────────────┘
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Requirements

- Java 8 or higher
- No external dependencies required

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Issues & Support

- Report bugs: [GitHub Issues](https://github.com/hizinhey/auto-cron-scheduler/issues)
- Feature requests: [GitHub Discussions](https://github.com/hizinhey/auto-cron-scheduler/discussions)
- Documentation: [Wiki](https://github.com/hizinhey/auto-cron-scheduler/wiki)

## ⭐ Show Your Support

If this project helped you, please give it a ⭐ star on GitHub!

---

**Made with ❤️ by [Hizin Hey](https://github.com/hizinhey)**
