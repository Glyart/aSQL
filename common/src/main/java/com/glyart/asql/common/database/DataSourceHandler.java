package com.glyart.asql.common.database;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the interaction logic for a data source.
 *
 * <p>Different implementations may change the logic of methods {@link #open()} and {@link #close()}.
 *
 * <p><b>NOTE: implementations should do a little exception (although it may not be necessary) handling for methods {@link #open()} and {@link #close()}, when
 * the adopted strategy is {@link Strategy#CONNECTION_POOL}.</b></p>
 *
 * <p>This is because the DataTemplate class cannot handle these methods when they are dealing with a connection pool.</p>
 *
 * <p>This is not needed when the adopted strategy is {@link Strategy#SIMPLE_CONNECTION}:
 * those methods will be managed by aSQL DataTemplate and possible exceptions will be passed to a CompletableFuture for further analysis by the user.</p>
 * @see DataSourceCredentials
 * @see Strategy
 * @see DataTemplate
 */
public interface DataSourceHandler {

    /**
     * Gets the existing connection to a data source.
     * @return The connection to a data source
     * @throws SQLException if something gone wrong while attempting to get the connection (no need to catch)
     */
    @NotNull
    Connection getConnection() throws SQLException;

    /**
     * Opens the connection to a data source. <br>
     * It can be used for opening connection pools.
     * @throws SQLException if something gone wrong while attempting to perform the implemented open operation.
     * No need to catch with {@link Strategy#SIMPLE_CONNECTION}
     */
    void open() throws SQLException;

    /**
     * Closes the connection to a data source. <br>
     * It can be used for closing connection pools.
     * @throws SQLException if something gone wrong while attempting to perform the implemented close operation.
     * No need to catch with {@link Strategy#SIMPLE_CONNECTION}
     */
    void close() throws SQLException;

    /**
     * Gets the implementation's adopted strategy.
     * @return the adopted strategy for connection management
     * @see Strategy
     */
    @NotNull
    Strategy getStrategy();
}
