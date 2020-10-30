package com.glyart.asql.common.defaults;

import com.glyart.asql.common.functions.PreparedStatementCreator;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DefaultCreator implements PreparedStatementCreator {

    @NotNull
    private final String sql;
    private final boolean getGeneratedKeys;

    public DefaultCreator(@NotNull String sql) {
        this(sql, false);
    }

    public DefaultCreator(@NotNull String sql, boolean getGeneratedKeys) {
        Preconditions.checkNotNull(sql, "Sql statement cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql statement cannot be empty.");
        this.sql = sql;
        this.getGeneratedKeys = getGeneratedKeys;
    }

    @Override
    public PreparedStatement createPreparedStatement(@NotNull Connection connection) throws SQLException {
        Preconditions.checkNotNull(connection, "Connection cannot be null.");
        return connection.prepareStatement(sql, getGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}