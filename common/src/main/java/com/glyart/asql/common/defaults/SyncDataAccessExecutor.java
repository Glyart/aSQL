package com.glyart.asql.common.defaults;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.database.DataAccessExecutor;
import com.glyart.asql.common.functions.PreparedStatementCallback;
import com.glyart.asql.common.functions.PreparedStatementCreator;
import com.glyart.asql.common.functions.StatementCallback;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class SyncDataAccessExecutor extends DataAccessExecutor<ASQLContext<?>> {

    public SyncDataAccessExecutor(ASQLContext<?> context) {
        super(context);
    }

    /**
     * Executes a synchronous JDBC data access operation, implemented as {@link StatementCallback} callback, using an
     * active connection.
     * The callback CAN return a result object (if it exists), for example a single object or a collection of objects.
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    @Override
    public <S> CompletableFuture<S> execute(@NotNull StatementCallback<S> callback) {
        Preconditions.checkNotNull(callback, "StatementCallback cannot be null.");

        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        Connection connection = getConnection();
        if (connection != null) {
            doStatementExecute(connection, callback, completableFuture);
        } else {
            completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
            logger.severe("Could not retrieve a connection.");
        }

        return completableFuture;
    }

    /**
     * Executes a synchronous JDBC data access operation, implemented as {@link PreparedStatementCallback} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param creator a callback that creates a PreparedStatement object given a connection
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    @Override
    public <S> CompletableFuture<S> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementCallback<S> callback) {
        Preconditions.checkNotNull(creator, "PreparedStatementCreator cannot be null.");
        Preconditions.checkNotNull(callback, "PreparedStatementCallback cannot be null.");

        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        Connection connection = getConnection();
        if (connection != null) {
            doPreparedStatementExecute(connection, creator, callback, completableFuture);
        } else {
            completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
            logger.severe("Could not retrieve a connection.");
        }
       
        return completableFuture;
    }

}
