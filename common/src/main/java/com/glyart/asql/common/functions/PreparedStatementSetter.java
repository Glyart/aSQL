package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultSetter;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a callback interface used by {@link DataTemplate}.
 *
 * <p>Implementations of this interface set values on a {@link PreparedStatement} provided by the DataTemplate class.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 *
 * <p>A default implementation called DefaultSetter is already provided.</p>
 * @see DefaultSetter
 */
@FunctionalInterface
public interface PreparedStatementSetter {

    /**
     * Sets parameter values into the given active PreparedStatement.
     * @param ps the PreparedStatement to invoke setter methods on
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(@NotNull PreparedStatement ps) throws SQLException;

}
