package com.glyart.asql.common.database;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.defaults.DefaultCreator;
import com.glyart.asql.common.functions.PreparedStatementCallback;
import com.glyart.asql.common.functions.PreparedStatementCreator;
import com.glyart.asql.common.functions.StatementCallback;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * <p>Represents a handling system for data access operations.</p>
 * <p>This class is used by {@link DataTemplate} to execute data access operations.</p>
 * @param <T> the ASQLContext which created this DataAccessExecutor
 */
@SuppressWarnings("unused")
public abstract class DataAccessExecutor<T extends ASQLContext<?>> {

    protected final T context;
    protected final Logger logger;

    public DataAccessExecutor(T context) {
        Preconditions.checkNotNull(context, "Context cannot be null.");
        this.context = context;
        this.logger = context.getLogger();
    }
    
    /**
     * Executes a JDBC data access operation, implemented as {@link StatementCallback} callback, using an
     * active connection.
     * The callback CAN return a result object (if it exists), for example a single object or a collection of objects.
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    public abstract <S> CompletableFuture<S> execute(@NotNull StatementCallback<S> callback);

    /**
     * Executes a JDBC data access operation, implemented as {@link PreparedStatementCallback} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param creator a callback that creates a PreparedStatement object given a connection
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    public abstract <S> CompletableFuture<S> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementCallback<S> callback);

    /**
     * Executes a JDBC data access operation, implemented as {@link PreparedStatementCallback} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param sql the SQL statement to execute
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    public <S> CompletableFuture<S> execute(@NotNull String sql, @NotNull PreparedStatementCallback<S> callback) {
        return execute(new DefaultCreator(sql), callback);
    }

    protected <S> void doStatementExecute(Connection connection, StatementCallback<S> callback, CompletableFuture<S> future) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            S result = callback.doInStatement(statement);
            future.complete(result);
        } catch (SQLException e) {
            future.completeExceptionally(e);
        } finally {
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    protected <S> void doPreparedStatementExecute(Connection connection, PreparedStatementCreator creator, PreparedStatementCallback<S> callback, CompletableFuture<S> future) {
        PreparedStatement ps = null;
        try {
            ps = creator.createPreparedStatement(connection);
            future.complete(callback.doInPreparedStatement(ps));
        } catch (SQLException e) {
            future.completeExceptionally(e);
        } finally {
            closeStatement(ps);
            closeConnection(connection);
        }
    }

    /**
     * Gets a connection using {@link DataSourceHandler}'s implementation.
     * This method's failures are fatal and definitely blocks {@link DataTemplate}'s access operations.
     * If that occurs then an error will be logged.
     * @return an active connection ready to be used (if it's available)
     */
    @Nullable
    protected Connection getConnection() {
        DataSourceHandler handler = context.getDataSourceHandler();
        Connection connection = null;
        try {
            if (handler.getStrategy() == Strategy.SIMPLE_CONNECTION)
                handler.open(); // This handler doesn't manage a connection pool so we must open a connection first
            else
                connection = context.getDataSourceHandler().getConnection(); // Retrieving connection from the opened connection pool
        } catch (SQLException e) {
            return null;
        }
        return connection;
    }

    /**
     * Gets a connection using {@link DataSourceHandler}'s implementation.
     * The connection will be available by accessing the returned CompletableFuture object,
     * with {@link CompletableFuture#whenComplete(BiConsumer)} method. Possible exceptions
     * are stored inside that object and they can also be accessed by using whenComplete method.
     * @return a never null CompletableFuture object which holds: an active connection ready to be used (if it's available)
     * @see CompletableFuture#whenComplete(BiConsumer)
     * @see CompletableFuture
     */
    @NotNull
    protected CompletableFuture<Connection> getFutureConnection() {
        CompletableFuture<Connection> futureConnection = new CompletableFuture<>();
        context.getScheduler().async(() -> {
            DataSourceHandler handler = context.getDataSourceHandler();
            Connection connection = null;
            try {
                if (handler.getStrategy() == Strategy.SIMPLE_CONNECTION)
                    handler.open(); // This handler doesn't manage a connection pool so we must open a connection first
                else
                    connection = context.getDataSourceHandler().getConnection(); // Retrieving connection from the opened connection pool
            } catch (SQLException e) {
                futureConnection.completeExceptionally(e);
            }
            futureConnection.complete(connection);
        });
        return futureConnection;
    }
    
    /**
     * Closes a connection using {@link DataSourceHandler}'s implementation.
     * A failure from this method is not fatal but it will be logged as warning.
     * @param connection the connection to close
     */
    protected void closeConnection(@Nullable Connection connection) {
        if (connection == null)
            return;

        DataSourceHandler handler = context.getDataSourceHandler();
        try {
            if (handler.getStrategy() == Strategy.SIMPLE_CONNECTION)
                handler.close(); // A simple connection based handler must decide how to manage connection closure
            else
                connection.close(); // We mustn't close the connection pool but we can free the created connection
        } catch (SQLException e) {
            logger.warning(() -> e.getMessage() + " - Error code: " + e.getErrorCode());
        }
    }

    /**
     * Tries to close a statement (accepts PreparedStatement objects).
     * A failure from this method is not fatal but it will be logged as warning.
     * @param statement the statement to close
     */
    protected void closeStatement(@Nullable Statement statement) {
        if (statement == null)
            return;

        try {
            statement.close();
        } catch (SQLException e) {
            logger.warning(() -> e.getMessage() + " - Error code: " + e.getErrorCode());
        }
    }

    /**
     * Tries to close a ResultSet object.
     * A failure from this method is not fatal but it will be logged as warning.
     * @param set the ResultSet to close
     */
    public void closeResultSet(@Nullable ResultSet set) {
        if (set == null)
            return;

        try {
            set.close();
        } catch (SQLException e) {
            logger.warning(() -> e.getMessage() + " - Error code: " + e.getErrorCode());
        }
    }
    
}
