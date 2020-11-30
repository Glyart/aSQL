package com.glyart.asql.velocity;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.context.ContextScheduler;
import com.glyart.asql.common.database.DataSourceCredentials;
import com.glyart.asql.common.database.DataSourceHandler;
import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultDataSourceHandler;
import com.glyart.asql.common.defaults.SyncDataAccessExecutor;
import com.google.common.base.Preconditions;
import com.velocitypowered.api.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Represents a modelled ASQLContext inside a Velocity Context. There can be multiple VelocityASQLContext.
 * Creating different VelocityASQLContext means that in the same Velocity Context you are willing
 * to interact with different data sources.
 */
public class VelocityASQLContext implements ASQLContext<Object> {

    private static ASQLVelocityScheduler<?> asqlVelocityScheduler;

    private final ProxyServer server;
    private final Object plugin;
    private final Logger logger;
    private final DataSourceCredentials credentials;
    private final DataSourceHandler dataSourceHandler;
    private final DataTemplate<VelocityASQLContext> dataTemplate;

    protected VelocityASQLContext(ProxyServer server, Object plugin, Logger logger, DataSourceCredentials credentials, DataSourceHandler dataSourceHandler) {
        this.server = server;
        this.plugin = plugin;
        this.logger = logger;
        this.credentials = credentials;
        this.dataSourceHandler = dataSourceHandler == null ? new DefaultDataSourceHandler(credentials, null) : dataSourceHandler;
        if (asqlVelocityScheduler == null)
            asqlVelocityScheduler = new ASQLVelocityScheduler<>(server, plugin);

        this.dataTemplate = new DataTemplate<>(new SyncDataAccessExecutor(this));
    }

    @Override
    public ContextScheduler getScheduler() {
        return asqlVelocityScheduler;
    }

    @Override
    public DataTemplate<? extends ASQLContext<Object>> getAsyncDataTemplate() {
        return dataTemplate;
    }

    @Override
    public DataTemplate<? extends ASQLContext<Object>> getSyncDataTemplate() {
        throw new UnsupportedOperationException("This context doesn't support sync data template");
    }

    @Override
    public Object getPlugin() {
        return plugin;
    }

    @Override
    public DataSourceHandler getDataSourceHandler() {
        return dataSourceHandler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public static ContextBuilder builder() {
        return new ContextBuilder();
    }

    public static class ContextBuilder {

        private ProxyServer server;
        private Object plugin;
        private Logger logger;
        private DataSourceCredentials credentials;
        private DataSourceHandler dataSourceHandler;

        private ContextBuilder() {

        }


        /**
         * Sets the ProxyServer of this VelocityASQLContext
         * @param server The ProxyServer of this Velocity proxy
         * @return This ContextBuilder instance
         */
        public ContextBuilder setServer(@NotNull ProxyServer server) {
            Preconditions.checkNotNull(plugin, "ProxyServer cannot be null.");
            this.server = server;
            return this;
        }
        
        /**
         * Sets the Object plugin which created this VelocityASQLContext.
         * @param plugin the EXISTING Object plugin which created this VelocityASQLContext
         * @return This ContextBuilder instance
         */
        public ContextBuilder setPlugin(@NotNull Object plugin) {
            Preconditions.checkNotNull(plugin, "Object plugin cannot be null.");
            this.plugin = plugin;
            return this;
        }

        /**
         * Sets the Logger that should use this VelocityASQLContext
         * @param logger The Logger
         * @return This ContextBuilder instance
         */
        public ContextBuilder setLogger(@NotNull Logger logger) {
            Preconditions.checkNotNull(logger, "Logger cannot be null.");
            this.logger = logger;
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
         * Builds a new VelocityASQLContext.
         * @return a new instance of VelocityASQLContext
         */
        @NotNull
        public VelocityASQLContext build() {
            return new VelocityASQLContext(server, plugin, logger, credentials, dataSourceHandler);
        }
    }

}
