package com.glyart.asql.common.defaults;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.database.DataAccessExecutor;
import com.glyart.asql.common.functions.PreparedStatementCallback;
import com.glyart.asql.common.functions.PreparedStatementCreator;
import com.glyart.asql.common.functions.StatementCallback;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class SyncDataAccessExecutor extends DataAccessExecutor<ASQLContext<?>> {

    public SyncDataAccessExecutor(ASQLContext context) {
        super(context);
    }

    @Override
    public <S> CompletableFuture<S> execute(@NotNull StatementCallback<S> callback) {
        Preconditions.checkNotNull(callback, "StatementCallback cannot be null.");
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        
        Connection connection = getConnection();
        if (connection != null) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                S result = callback.doInStatement(statement);
                completableFuture.complete(result);
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            } finally {
                closeStatement(statement);
                closeConnection(connection);
            }
        } else {
            completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
            logger.severe("Could not retrieve a connection.");
        }

        return completableFuture;
    }

    @Override
    public <S> CompletableFuture<S> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementCallback<S> callback) {
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        Connection connection = getConnection();
        
        if (connection != null) {
            PreparedStatement ps = null;
            try {
                ps = creator.createPreparedStatement(connection);
                completableFuture.complete(callback.doInPreparedStatement(ps));
            } catch (SQLException e) {
                completableFuture.completeExceptionally(e);
            } finally {
                closeStatement(ps);
                closeConnection(connection);
            }
        } else {
            completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
            logger.severe("Could not retrieve a connection.");
        }
       
        return completableFuture;
    }

}
