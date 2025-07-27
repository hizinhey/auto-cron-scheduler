package org.sol.hizinhey.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CronJob {
    /**
     * Cron expression (e.g., "0 0 12 * * ?" for daily at noon)
     * Format: second minute hour day month dayOfWeek
     */
    String value();

    /**
     * Optional name for the job
     */
    String name() default "";

    /**
     * Whether the job should start immediately when scheduler starts
     */
    boolean startImmediately() default false;
}