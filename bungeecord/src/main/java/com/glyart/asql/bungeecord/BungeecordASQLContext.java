package com.glyart.asql.bungeecord;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.context.ContextScheduler;
import com.glyart.asql.common.database.DataSourceCredentials;
import com.glyart.asql.common.database.DataSourceHandler;
import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultDataSourceHandler;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Represents a modelled ASQLContext inside a Bungeecord Context. There can be multiple BungeecordASQLContext.
 * Creating different BungeecordASQLContext means that in the same Bungeecord Context you are willing
 * to interact with different data sources.
 */
public class BungeecordASQLContext implements ASQLContext<Plugin> {

    private static ASQLBungeecordScheduler<? extends Plugin> asqlBungeecordScheduler;

    private final Plugin plugin;
    private final DataSourceCredentials credentials;
    private final DataSourceHandler dataSourceHandler;
    private final DataTemplate<BungeecordASQLContext> dataTemplate;

    protected BungeecordASQLContext(Plugin plugin, DataSourceCredentials credentials, DataSourceHandler dataSourceHandler) {
        this.plugin = plugin;
        this.credentials = credentials;
        this.dataSourceHandler = dataSourceHandler == null ? new DefaultDataSourceHandler(credentials, null) : dataSourceHandler;
        if (asqlBungeecordScheduler == null)
            asqlBungeecordScheduler = new ASQLBungeecordScheduler<>(plugin);

        this.dataTemplate = new DataTemplate<>(this);
    }

    @Override
    public ContextScheduler getScheduler() {
        return asqlBungeecordScheduler;
    }

    @Override
    public DataTemplate<BungeecordASQLContext> getDataTemplate() {
        return dataTemplate;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public DataSourceHandler getDataSourceHandler() {
        return dataSourceHandler;
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public static class ContextBuilder {

        private Plugin plugin;
        private DataSourceCredentials credentials;
        private DataSourceHandler dataSourceHandler;

        private ContextBuilder() {

        }

        /**
         * Sets the Plugin which created this BungeecordASQLContext.
         * @param plugin the EXISTING Plugin which created this BungeecordASQLContext
         * @return This ContextBuilder instance
         */
        public ContextBuilder setPlugin(@NotNull Plugin plugin) {
            Preconditions.checkNotNull(plugin, "Plugin cannot be null.");
            this.plugin = plugin;
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
         * Builds a new BungeecordASQLContext.
         * @return a new instance of BungeecordASQLContext
         */
        @NotNull
        public BungeecordASQLContext build() {
            return new BungeecordASQLContext(plugin, credentials, dataSourceHandler);
        }
    }

}
