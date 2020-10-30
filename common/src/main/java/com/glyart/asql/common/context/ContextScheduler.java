package com.glyart.asql.common.context;

import lombok.NonNull;

/**
 * Represents a scheduling behavior.
 */
public interface ContextScheduler {

    /**
     * Executes an asynchronous repeating task every given ticks, after the given ticks delay time.
     * @param runnable The task to run
     * @param delay the ticks to wait before running the task for the first time
     * @param period the ticks to wait before each run
     */
    void async(@NonNull Runnable runnable, long delay, long period);

    /**
     * Executes an asynchronous task which will be only ran once.
     * @param runnable The task to run
     */
    void async(@NonNull Runnable runnable);


    /**
     * Executes a synchronous repeating task every given ticks, after the given ticks delay time.
     * @param runnable The task to run
     * @param delay the ticks to wait before running the task for the first time
     * @param period the ticks to wait before each run
     */
    void sync(@NonNull Runnable runnable, long delay, long period);

    /**
     * Executes an asynchronous task which will be only ran once.
     * @param runnable The task to run
     */
    void sync(@NonNull Runnable runnable);

}
