package com.glyart.asql.common.defaults;

import com.glyart.asql.common.database.DataSourceCredentials;
import com.glyart.asql.common.database.DataSourceHandler;
import com.glyart.asql.common.database.Strategy;
import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class DefaultDataSourceHandler implements DataSourceHandler {

    private final DataSourceCredentials credentials;

    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    public DefaultDataSourceHandler(@NotNull DataSourceCredentials credentials, @Nullable String poolName) {
        Preconditions.checkNotNull(credentials, "Credentials cannot be null.");
        this.credentials = credentials;
        initConfig(poolName);
    }

    private void initConfig(String poolName) {
        if (credentials.getProperties() == null) {
            hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%s", credentials.getHostname(), credentials.getPort()));
            hikariConfig.setUsername(credentials.getUsername());
            hikariConfig.setPassword(credentials.getPassword());
            hikariConfig.setSchema(credentials.getDatabase());
            if (poolName != null)
                hikariConfig.setPoolName(poolName);

            return;
        }

        hikariConfig = new HikariConfig(credentials.getProperties());
    }

    @NotNull
    @Override
    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public void open() {
        if (hikariDataSource == null || hikariDataSource.isClosed())
            hikariDataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        hikariDataSource.close();
    }

    @NotNull
    @Override
    public Strategy getStrategy() {
        return Strategy.CONNECTION_POOL;
    }

}
