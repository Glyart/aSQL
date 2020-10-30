package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Represents a callback interface used by the {@link DataTemplate} class for executing batch updates.
 *
 * <p>Implementations of this interface set values on a {@link PreparedStatement} provided by the DataTemplate class, for each
 * of a number of updates in a batch using the same SQL statement.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 * @param <T> yhe argument type
 * @see DataTemplate#batchUpdate(String, List, ParametrizedPreparedStatementSetter)
 */
public interface ParametrizedPreparedStatementSetter<T> {

    /**
     * Sets the parameter values of the T argument inside the PreparedStatement
     * @param ps an active PreparedStatement
     * @param argument a generic object containing the values to set
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(PreparedStatement ps, T argument) throws SQLException;

}
