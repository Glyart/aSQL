package com.glyart.asql.spigot;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.context.ContextScheduler;
import com.glyart.asql.common.database.DataSourceCredentials;
import com.glyart.asql.common.database.DataSourceHandler;
import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.database.AsyncDataAccessExecutor;
import com.glyart.asql.common.defaults.DefaultDataSourceHandler;
import com.glyart.asql.common.database.SyncDataAccessExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Represents a modelled ASQLContext inside a Spigot Context. There can be multiple SpigotASQLContext.
 * Creating different SpigotASQLContext means that in the same Spigot Context you are willing
 * to interact with different data sources.
 */
public class SpigotASQLContext implements ASQLContext<JavaPlugin> {

    private static ASQLSpigotScheduler<? extends JavaPlugin> asqlSpigotScheduler;

    private final JavaPlugin javaPlugin;
    private final DataSourceCredentials credentials;
    private final DataSourceHandler dataSourceHandler;
    private final DataTemplate<AsyncDataAccessExecutor> asyncDataTemplate;
    private final DataTemplate<SyncDataAccessExecutor> syncDataTemplate;

    protected SpigotASQLContext(JavaPlugin javaPlugin, DataSourceCredentials credentials, DataSourceHandler dataSourceHandler) {
        this.javaPlugin = javaPlugin;
        this.credentials = credentials;
        this.dataSourceHandler = dataSourceHandler == null ? new DefaultDataSourceHandler(credentials, null) : dataSourceHandler;
        if (asqlSpigotScheduler == null)
            asqlSpigotScheduler = new ASQLSpigotScheduler<>(javaPlugin);

        this.asyncDataTemplate = new DataTemplate<>(new AsyncDataAccessExecutor(this));
        this.syncDataTemplate = new DataTemplate<>(new SyncDataAccessExecutor(this));
    }

    @Override
    public ContextScheduler getScheduler() {
        return asqlSpigotScheduler;
    }

    @Override
    public DataTemplate<AsyncDataAccessExecutor> getAsyncDataTemplate() {
        return asyncDataTemplate;
    }

    @Override
    public DataTemplate<SyncDataAccessExecutor> getSyncDataTemplate() {
        return syncDataTemplate;
    }

    @Override
    public JavaPlugin getPlugin() {
        return javaPlugin;
    }

    @Override
    public DataSourceHandler getDataSourceHandler() {
        return dataSourceHandler;
    }

    @Override
    public Logger getLogger() {
        return javaPlugin.getLogger();
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public static class ContextBuilder {

        private JavaPlugin javaPlugin;
        private DataSourceCredentials credentials;
        private DataSourceHandler dataSourceHandler;

        private ContextBuilder() {

        }

        /**
         * Sets the JavaPlugin which created this SpigotASQLContext.
         * @param javaPlugin the EXISTING JavaPlugin which created this SpigotASQLContext
         * @return This ContextBuilder instance
         */
        public ContextBuilder setPlugin(@NotNull JavaPlugin javaPlugin) {
            Preconditions.checkNotNull(javaPlugin, "JavaPlugin cannot be null.");
            this.javaPlugin = javaPlugin;
            return this;
        }

        /**
         * Sets the given credentials for connecting to a data source.
         * @param credentials The credentials for connecting to a data source
         * @return This ContextBuilder instance
         */
        public ContextBuilder setCredentials(@NotNull DataSourceCredentials credentials) {
            Preconditions.checkNotNull(credentials, "DataSourceCredentials cannot be null.");
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the handler for the data source interaction. If it's not provided then a Hikari based default implementation will be used.
         * @param dataSourceHandler The handler for the data source interaction.
         * @return This ContextBuilder instance
         * @see DefaultDataSourceHandler
         */
        public ContextBuilder setDatabaseHandler(@Nullable DataSourceHandler dataSourceHandler) {
            this.dataSourceHandler = dataSourceHandler;
            return this;
        }

        /**
         * Builds a new SpigotASQLContext.
         * @return a new instance of SpigotASQLContext
         */
        @NotNull
        public SpigotASQLContext build() {
            return new SpigotASQLContext(javaPlugin, credentials, dataSourceHandler);
        }
    }

}
