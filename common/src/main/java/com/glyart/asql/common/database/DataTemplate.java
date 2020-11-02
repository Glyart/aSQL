package com.glyart.asql.common.database;

import com.glyart.asql.common.context.ASQLContext;
import com.glyart.asql.common.defaults.DefaultBatchSetter;
import com.glyart.asql.common.defaults.DefaultCreator;
import com.glyart.asql.common.defaults.DefaultExtractor;
import com.glyart.asql.common.defaults.DefaultSetter;
import com.glyart.asql.common.functions.*;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Represents the final interaction to a data source.
 * <p>An instance of the DataTemplate class is linked to the ASQLContext which provided its instance.
 * Knowing that, the DataTemplate class can <i>access and use</i> that ASQLContext members.</p>
 *
 * <p>This class:
 * <ul>
 *     <li>works asynchronously, without overhead on the main thread</li>
 *     <li>executes all the <a href="https://en.wikipedia.org/wiki/Create,_read,_update_and_delete">CRUD</a> operations on a data source</li>
 *     <li>handles exceptions</li>
 *     <li>gives not null <b>{@link CompletableFuture} objects that WILL STORE usable future results</b></li>
 *     <li>iterates over ResultSets</li>
 *     <li>deals with static and prepared statements</li>
 * </ul>
 *
 * <p>Methods of this class use various callback interfaces. A reading of those is greatly suggested.
 *
 * <p>There shouldn't be the need for using the public constructor. Getting an instance of this class by using {@link ASQLContext#getDataTemplate()}
 * should be enough.
 * <br> Since the callback interfaces make DataTemplate's methods parameterizable, there should be no need to subclass DataTemplate.
 * @param <T> The context who created this data template
 * @see CompletableFuture
 * @see BatchPreparedStatementSetter
 * @see ParametrizedPreparedStatementSetter
 * @see PreparedStatementCallback
 * @see PreparedStatementCreator
 * @see ResultSetExtractor
 * @see RowMapper
 * @see StatementCallback
 */
@SuppressWarnings("unused")
public class DataTemplate<T extends ASQLContext<?>> {

    private final T context;
    private final Logger logger;

    public DataTemplate(T context) {
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
    public <S> CompletableFuture<S> execute(@NotNull StatementCallback<S> callback) {
        Preconditions.checkNotNull(callback, "StatementCallback cannot be null.");
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        context.getScheduler().async(() -> {
            Connection connection = getConnection();
            if (connection == null) {
                completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
                logger.severe("Could not retrieve a connection.");
                return;
            }
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
        });

        return completableFuture;
    }

    /**
     * Performs a single update operation (like insert, delete, update).
     * @param sql static SQL statement to execute
     * @param getGeneratedKeys a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows. 
     * If getGeneratedKeys is true, this method will return the key of the new generated row
     */
    public CompletableFuture<Integer> update(@NotNull String sql, boolean getGeneratedKeys) {
        Preconditions.checkNotNull(sql, "Sql statement cannot be null.");

        return execute(statement -> {
            int rows;
            ResultSet set = null;
            try {
                if (getGeneratedKeys) {
                    statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                    set = statement.getGeneratedKeys();
                    rows = set.next() ? set.getInt(1) : 0;
                } else
                    rows = statement.executeUpdate(sql, Statement.NO_GENERATED_KEYS);
            } finally {
                closeResultSet(set);
            }
            return rows;
        });
    }

    /**
     * Executes a query given static SQL statement, then it reads the {@link ResultSet} using the {@link ResultSetExtractor} implementation.
     * @param sql the query to execute
     * @param extractor a callback that will extract all rows from the ResultSet
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     */
    public <S> CompletableFuture<S> query(@NotNull String sql, ResultSetExtractor<S> extractor) {
        return execute(statement -> {
            ResultSet resultSet = null;
            S result;
            try {
                resultSet = statement.executeQuery(sql);
                result = extractor.extractData(resultSet);
            } finally {
                closeResultSet(resultSet);
            }
            return result;
        });
    }

    /**
     * Executes a query given static SQL statement, then it maps each
     * ResultSet row to a result object using the {@link RowMapper} implementation.
     * @param sql the query to execute
     * @param rowMapper a callback that will map one object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     */
    public <S> CompletableFuture<List<S>> queryForList(@NotNull String sql, RowMapper<S> rowMapper) {
        return query(sql, new DefaultExtractor<>(rowMapper));
    }

    /**
     * Executes a query given static SQL statement, then it maps the first
     * ResultSet row to a result object using the {@link RowMapper} implementation.
     *
     * <p>Note: use of this method is discouraged when the query doesn't supply exactly one row.
     * If more rows are supplied then this method will return only the first one.</p>
     * @param sql the query to execute
     * @param rowMapper a callback that will map the object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exists)
     */
    public <S> CompletableFuture<S> queryForObject(@NotNull String sql, RowMapper<S> rowMapper) {
        CompletableFuture<S> futureSinglet = new CompletableFuture<>();
        context.getScheduler().async(() -> {
            CompletableFuture<List<S>> listCompletableFuture = query(sql, new DefaultExtractor<>(rowMapper, 1));
            listCompletableFuture.whenComplete((s, throwable) -> futureSinglet.complete(s.isEmpty() ? null : s.get(0)));
        });
        return futureSinglet;
    }

    /**
     * Executes a JDBC data access operation, implemented as {@link PreparedStatementCallback} callback
     * working on a PreparedStatement.
     * The callback CAN return a result object (if it exists), for example a singlet or a collection of objects.
     * @param creator a callback that creates a PreparedStatement object given a connection
     * @param callback a callback that holds the operation logic
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: an object returned by the callback, or null if it's not available
     */
    public <S> CompletableFuture<S> execute(@NotNull PreparedStatementCreator creator, @NotNull PreparedStatementCallback<S> callback) {
        CompletableFuture<S> completableFuture = new CompletableFuture<>();
        context.getScheduler().async(() -> {
            Connection connection = getConnection();
            if (connection == null) {
                completableFuture.completeExceptionally(new SQLException("Could not retrieve connection."));
                logger.severe("Could not retrieve a connection.");
                return;
            }
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
        });
        return completableFuture;
    }

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

    /**
     * Performs a single update operation (like insert, delete, update) using a {@link PreparedStatementCreator} to provide SQL
     * and any required parameters. A {@link PreparedStatementSetter} can be passed as helper that sets bind parameters.
     * @param creator a callback that provides the PreparedStatement with bind parameters
     * @param setter a helper that sets bind parameters. If it's null then this will be an update with static SQL
     * @param getGeneratedKey a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows. 
     * If getGeneratedKeys is true, this method will return the key of the new generated row
     */
    public CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) {
        return execute(creator, ps -> {
            ResultSet set = null;
            int rows;
            try {
                if (setter != null)
                    setter.setValues(ps);

                if (getGeneratedKey) {
                    ps.executeUpdate();
                    set = ps.getGeneratedKeys();
                    rows = set.next() ? set.getInt(1) : 0;
                } else
                    rows = ps.executeUpdate();
            } finally {
                closeResultSet(set);
            }
            return rows;
        });
    }

    /**
     * Performs a single update operation (like insert, delete, update) using a {@link PreparedStatementCreator} to
     * to provide SQL and any required parameters.
     * @param creator a callback that provides the PreparedStatement with required parameters
     * @param getGeneratedKeys a boolean values
     * @return a never null CompletableFuture object which holds: the number of the affected rows. 
     * If getGeneratedKeys is true, this method will return the key of the new generated row
     */
    public CompletableFuture<Integer> update(@NotNull PreparedStatementCreator creator, boolean getGeneratedKeys) {
        return update(creator, null, getGeneratedKeys);
    }

    /**
     * Performs a single update operation (like insert, delete, update).
     * A {@link PreparedStatementSetter} can be passed as helper that sets bind parameters.
     * @param sql the SQL containing bind parameters
     * @param setter a helper that sets bind parameters. If it's null then this will be an update with static SQL
     * @param getGeneratedKey a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows. 
     * If getGeneratedKeys is true, this method will return the key of the new generated row
     */
    public CompletableFuture<Integer> update(@NotNull String sql, @Nullable PreparedStatementSetter setter, boolean getGeneratedKey) {
        return update(new DefaultCreator(sql), setter, getGeneratedKey);
    }

    /**
     * Performs a single update operation (like insert, update or delete statement)
     * via PreparedStatement, binding the given parameters.
     * @param sql the SQL containing bind parameters
     * @param params arguments to be bind to the given SQL
     * @param getGeneratedKey a boolean value
     * @return a never null CompletableFuture object which holds: the number of the affected rows. 
     * If getGeneratedKeys is true, this method will return the key of the new generated row
     */
    public CompletableFuture<Integer> update(@NotNull String sql, Object[] params, boolean getGeneratedKey) {
        return update(sql, new DefaultSetter(params), getGeneratedKey);
    }

    /**
     * Performs multiple update operations using a single SQL statement.
     * <p><b>NOTE: this method will be unusable if the driver doesn't support batch updates.</b></p>
     * @param sql the SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchSetter a callback that sets parameters on the PreparedStatement created by this method
     * @return a CompletableFuture object. It can be used for knowing when the batch update is done and if an exception occurred
     * @throws IllegalStateException if the driver doesn't support batch updates
     * @see CompletableFuture#exceptionally(Function)
     * @see CompletableFuture#whenComplete(BiConsumer)
     * @see BatchPreparedStatementSetter
     */
    public CompletableFuture<Void> batchUpdate(@NotNull String sql, @NotNull BatchPreparedStatementSetter batchSetter) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql cannot be empty.");
        Preconditions.checkNotNull(batchSetter, "BatchPreparedStatementSetter cannot be null.");

        return execute(sql, ps -> {
            if (!ps.getConnection().getMetaData().supportsBatchUpdates())
                throw new IllegalStateException("This driver doesn't support batch updates. This method will remain unusable until you choose a driver that supports batch updates.");

            for (int i = 0; i < batchSetter.getBatchSize(); i++) {
                batchSetter.setValues(ps, i);
                ps.addBatch();
            }
            ps.executeBatch();
            return null;
        });
    }

    /**
     * Performs multiple update operations using a single SQL statement.
     * @param sql The SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchArgs A list of object arrays containing the batch arguments
     * @return A CompletableFuture object. It can be used for knowing when the batch update is done and if an exception occurred
     * @throws IllegalStateException If the driver doesn't support batch updates
     * @see CompletableFuture#isCompletedExceptionally()
     */
    public CompletableFuture<Void> batchUpdate(@NotNull String sql, @Nullable List<Object[]> batchArgs) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql cannot be empty.");
        if (batchArgs == null || batchArgs.isEmpty())
            return CompletableFuture.completedFuture(null);

        return batchUpdate(sql, new DefaultBatchSetter(batchArgs));
    }

    /**
     * Performs multiple update operations using a single SQL statement.
     * @param sql the SQL containing bind parameters. It will be
     * reused because all statements in a batch use the same SQL
     * @param batchArgs a list of objects containing the batch arguments
     * @param paramsBatchSetter a callback that sets parameters on the PreparedStatement created by this method
     * @param <S> the parameter type
     * @return a CompletableFuture object. It can be used for knowing when the batch update is done and if an exception occurred
     * @throws IllegalStateException if the driver doesn't support batch updates
     * @see ParametrizedPreparedStatementSetter
     */
    public <S> CompletableFuture<Void> batchUpdate(@NotNull String sql, @Nullable List<S> batchArgs, @NotNull ParametrizedPreparedStatementSetter<S> paramsBatchSetter) throws IllegalStateException {
        Preconditions.checkNotNull(sql, "Sql cannot be null.");
        Preconditions.checkArgument(!sql.isEmpty(), "Sql cannot be empty.");
        Preconditions.checkNotNull(paramsBatchSetter, "ParametrizedPreparedStatementSetter cannot be null.");

        if (batchArgs == null || batchArgs.isEmpty())
            return CompletableFuture.completedFuture(null);

        return execute(sql, ps -> {
            if (!ps.getConnection().getMetaData().supportsBatchUpdates())
                throw new IllegalStateException("This driver doesn't support batch updates. This method will remain unusable until you choose a driver that supports batch updates.");

            for (S batchParam : batchArgs) {
                paramsBatchSetter.setValues(ps, batchParam);
                ps.addBatch();
            }
            ps.executeBatch();
            return null;
        });
    }

    /**
     * Executes a query using a PreparedStatement, created by a {@link PreparedStatementCreator} and with his values set
     * by a {@link PreparedStatementSetter}.
     *
     * <p>Most other query methods use this method, but application code will always
     * work with either a creator or a setter.</p>
     * @param creator a callback that creates a PreparedStatement
     * @param setter a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param extractor a callback that will extract results given a ResultSet
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @see PreparedStatementSetter
     */
    public <S> CompletableFuture<S> query(@NotNull PreparedStatementCreator creator, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<S> extractor) {
        Preconditions.checkNotNull(extractor, "ResultSetExtractor cannot be null.");

        return execute(creator, ps -> {
            S result;
            ResultSet resultSet = null;
            try {
                if (setter != null)
                    setter.setValues(ps);

                resultSet = ps.executeQuery();
                result = extractor.extractData(resultSet);
            } finally {
                closeResultSet(resultSet);
            }
            return result;
        });
    }

    /**
     * Executes a query using a PreparedStatement, then reading the ResultSet with a {@link ResultSetExtractor} implementation.
     * @param creator a callback that creates a PreparedStatement
     * @param extractor a callback that will extract results given a ResultSet
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     * @see PreparedStatementCreator
     */
    public <S> CompletableFuture<S> query(@NotNull PreparedStatementCreator creator, @NotNull ResultSetExtractor<S> extractor) {
        return query(creator, null, extractor);
    }

    /**
     * Executes a query using a PreparedStatement, mapping each row to a result object via a {@link RowMapper} implementation.
     * @param psc a callback that creates a PreparedStatement
     * @param rowMapper a callback that will map one object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     */
    public <S> CompletableFuture<List<S>> query(@NotNull PreparedStatementCreator psc, @NotNull RowMapper<S> rowMapper) {
        return query(psc, new DefaultExtractor<>(rowMapper));
    }

    /**
     * Executes a query using a SQL statement, then reading the ResultSet with a {@link ResultSetExtractor} implementation.
     * @param sql the query to execute
     * @param setter a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param extractor a callback that will extract results given a ResultSet
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     */
    public <S> CompletableFuture<S> query(@NotNull String sql, @Nullable PreparedStatementSetter setter, @NotNull ResultSetExtractor<S> extractor) {
        return query(new DefaultCreator(sql), setter, extractor);
    }

    /**
     * Executes a query using a SQL statement and a {@link PreparedStatementSetter} implementation that will bind values to the query.
     * Each row of the ResultSet will be map to a result object via a RowMapper implementation.
     * @param sql the query to execute
     * @param pss a callback that sets values on the PreparedStatement. If null, the SQL will be treated as static SQL with no bind parameters
     * @param rowMapper a callback that will map one object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     */
    public <S> CompletableFuture<List<S>> query(@NotNull String sql, @Nullable PreparedStatementSetter pss, @NotNull RowMapper<S> rowMapper) {
        return query(sql, pss, new DefaultExtractor<>(rowMapper));
    }

    /**
     * Executes a query given a SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * The {@link ResultSetExtractor} implementation will read the ResultSet.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param extractor a callback that will extract results given a ResultSet
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result object (if it exists), according to the ResultSetExtractor implementation
     */
    public <S> CompletableFuture<S> query(@NotNull String sql, @Nullable Object[] args, @NotNull ResultSetExtractor<S> extractor) {
        return query(sql, new DefaultSetter(args), extractor);
    }

    /**
     * Executes a query given a SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * Each row of the ResultSet will be map to a result object via a {@link RowMapper} implementation.
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param rowMapper a callback that will map one object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a result list containing mapped objects (if they exist)
     */
    public <S> CompletableFuture<List<S>> queryForList(@NotNull String sql, @Nullable Object[] args, @NotNull RowMapper<S> rowMapper) {
        return query(sql, args, new DefaultExtractor<>(rowMapper));
    }

    /**
     * Executes a query given a SQL statement: it will be used to create a PreparedStatement.
     * Then a list of arguments will be bound to the query.
     * Each row of the ResultSet will be map to a result object via a {@link RowMapper} implementation.
     *
     * <p>Note: use of this method is discouraged when the query doesn't supply exactly one row. 
     * If more rows are supplied then this method will return only the first one.</p>
     * @param sql the query to execute
     * @param args arguments to bind to the query
     * @param rowMapper a callback that will map one object per ResultSet row
     * @param <S> the result type
     * @return a never null CompletableFuture object which holds: a mapped result object (if it exist)
     */
    public <S> CompletableFuture<S> queryForObject(@NotNull String sql, Object[] args, @NotNull RowMapper<S> rowMapper) {
        CompletableFuture<S> futureSinglet = new CompletableFuture<>();
        context.getScheduler().async(() -> {
            CompletableFuture<List<S>> listCompletableFuture = query(sql, args, new DefaultExtractor<>(rowMapper, 1));
            listCompletableFuture.whenComplete((s, throwable) -> futureSinglet.complete(s.isEmpty() ? null : s.get(0)));
        });
        return futureSinglet;
    }

    /**
     * Gets a connection using {@link DataSourceHandler}'s implementation.
     * This method's failures are fatal and definitely blocks {@link DataTemplate}'s access operations.
     * If that occurs then an error will be logged.
     * @return an active connection ready to be used
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
     * @return a never null CompletableFuture object which holds: an active connection ready to be used
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
    protected void closeResultSet(@Nullable ResultSet set) {
        if (set == null)
            return;

        try {
            set.close();
        } catch (SQLException e) {
            logger.warning(() -> e.getMessage() + " - Error code: " + e.getErrorCode());
        }
    }
}
