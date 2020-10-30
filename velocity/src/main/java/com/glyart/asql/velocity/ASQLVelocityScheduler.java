package com.glyart.asql.velocity;

import com.glyart.asql.common.context.ContextScheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Represents a task scheduling behavior through a Velocity Context.
 * There should be just one instance of ASQLVelocityScheduler per Velocity Context.
 * @param <T> An existing Plugin
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ASQLVelocityScheduler<T> implements ContextScheduler {

    private final ProxyServer server;
    private final T plugin;

    /**
     * Executes an asynchronous repeating task every given milliseconds, after the given milliseconds delay time.
     * @param runnable The task to run
     * @param delay the ticks to wait before running the task for the first time
     * @param period the ticks to wait before each run
     */
    @Override
    public void async(@NonNull Runnable runnable, long delay, long period) {
        server.getScheduler().buildTask(plugin, runnable)
                .delay(delay, TimeUnit.MILLISECONDS)
                .repeat(period, TimeUnit.MILLISECONDS)
                .schedule();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void async(@NonNull Runnable runnable) {
        server.getScheduler().buildTask(plugin, runnable).schedule();
    }

    /**
     * Don't call this method. Sync context is unsupported on Velocity.
     * @throws UnsupportedOperationException if called because Velocity context doesn't support synchronous context
     */
    @Override
    public void sync(@NonNull Runnable runnable, long delay, long period) {
        throw new UnsupportedOperationException("The Velocity context doesn't support this operation");
    }

    /**
     * Don't call this method. Sync context is unsupported on Velocity.
     * @throws UnsupportedOperationException if called because Velocity context doesn't support synchronous context
     */
    @Override
    public void sync(@NonNull Runnable runnable) {
        throw new UnsupportedOperationException("The Velocity context doesn't support this operation");
    }
    
}
