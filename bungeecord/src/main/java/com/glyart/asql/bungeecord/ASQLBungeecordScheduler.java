package com.glyart.asql.bungeecord;

import com.glyart.asql.common.context.ContextScheduler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.concurrent.TimeUnit;

/**
 * Represents a task scheduling behavior through a Bungeecord Context.
 * There should be just one instance of ASQLBungeecordScheduler per Bungeecord Context.
 * @param <T> An existing Plugin
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ASQLBungeecordScheduler<T extends Plugin> implements ContextScheduler {

    private final T plugin;

    /**
     * Executes an asynchronous repeating task every given milliseconds, after the given milliseconds delay time.
     * @param runnable The task to run
     * @param delay the ticks to wait before running the task for the first time
     * @param period the ticks to wait before each run
     */
    @Override
    public void async(@NonNull Runnable runnable, long delay, long period) {
        plugin.getProxy().getScheduler().schedule(plugin, runnable, delay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void async(@NonNull Runnable runnable) {
        plugin.getProxy().getScheduler().runAsync(plugin, runnable);
    }

    /**
     * Don't call this method. Sync context is unsupported on Bungeecord.
     * @throws UnsupportedOperationException if called because Bungeecord context doesn't support synchronous context
     */
    @Override
    public void sync(@NonNull Runnable runnable, long delay, long period) {
        throw new UnsupportedOperationException("The Bungeecord context doesn't support this operation");
    }

    /**
     * Don't call this method. Sync context is unsupported on Bungeecord.
     * @throws UnsupportedOperationException if called because Bungeecord context doesn't support synchronous context
     */
    @Override
    public void sync(@NonNull Runnable runnable) {
        throw new UnsupportedOperationException("The Bungeecord context doesn't support this operation");
    }
    
}
