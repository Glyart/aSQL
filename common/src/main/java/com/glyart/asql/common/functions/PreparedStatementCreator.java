package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultCreator;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a callback interface used by multiple methods of the {@link DataTemplate} class.
 *
 * <p>Implementations create a PreparedStatement with a given active Connection, provided by the DataTemplate class. Still, they
 * are responsible for providing the SQL statement and any necessary parameters.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 *
 * <p>A default implementation called DefaultCreator is already provided.
 * @see DefaultCreator
 */
@FunctionalInterface
public interface PreparedStatementCreator {

    /**
     * Creates a PreparedStatement in this connection. There is no need to close the PreparedStatement:
     * the DataTemplate class will do that.
     * @param connection the connection for creating the PreparedStatement
     * @return a PreparedStatement object
     * @throws SQLException if something goes wrong during the PreparedStatement creation (no need to catch)
     */
    PreparedStatement createPreparedStatement(@NotNull Connection connection) throws SQLException;

}
