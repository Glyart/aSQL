package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultBatchSetter;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a batch update callback interface used by the {@link DataTemplate} class.
 *
 * <p>Implementations set values on a {@link PreparedStatement} provided by The DataTemplate class, for each
 * of a number of updates in a batch using the same SQL statement.
 * They are responsible for setting parameters: a SQL statement with placeholders (question marks) will already have been supplied.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 *
 * <p>A default implementation called DefaultBatchSetter is already provided.</p>
 * @see DefaultBatchSetter
 */
public interface BatchPreparedStatementSetter {

    /**
     * Sets parameter values on the given PreparedStatement.
     * @param ps an active PreparedStatement for invoking setter methods
     * @param i index of the statement inside the batch, starting from 0
     * @throws SQLException if an SQLException is encountered while trying to set values (no need to catch)
     */
    void setValues(@NotNull PreparedStatement ps, int i) throws SQLException;

    /**
     * Gets the size of the batch.
     * @return the number of statements in the batch
     */
    int getBatchSize();

}
