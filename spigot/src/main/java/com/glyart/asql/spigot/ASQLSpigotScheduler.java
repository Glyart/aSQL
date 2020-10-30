package com.glyart.asql.spigot;

import com.glyart.asql.common.context.ContextScheduler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a task scheduling behavior through a Spigot Context.
 * There should be just one instance of ASQLSpigotScheduler per Spigot Context.
 * @param <T> An existing JavaPlugin
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ASQLSpigotScheduler<T extends JavaPlugin> implements ContextScheduler {

    private final T plugin;

    /**
     * {@inheritDoc}
     */
    @Override
    public void async(@NonNull Runnable runnable, long delay, long period) {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void async(@NonNull Runnable runnable) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sync(@NonNull Runnable runnable, long delay, long period) {
        plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sync(@NonNull Runnable runnable) {
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

}
