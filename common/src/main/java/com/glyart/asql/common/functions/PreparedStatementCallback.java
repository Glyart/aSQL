package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Represents a callback interface for code that operates on a PreparedStatement.
 * This is internally used by the {@link DataTemplate} class but it's also useful for custom purposes.
 *
 * <p>Note: the passed-in PreparedStatement can have been created by aSQL or by a custom {@link PreparedStatementCreator} implementation.
 * However, the latter is hardly ever necessary, as most custom callback actions will perform updates
 * in which case a standard PreparedStatement is fine. Custom actions will
 * always set parameter values themselves, so that PreparedStatementCreator
 * capability is not needed either.</p>
 *
 * <p>Implementations don't need to worry about handling exceptions:
 * they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.
 * @param <T> The result type
 * @see DataTemplate#execute(String, PreparedStatementCallback)
 * @see DataTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)
 */
@FunctionalInterface
public interface PreparedStatementCallback<T> {

    /**
     * Gets called by {@link DataTemplate#execute(String, PreparedStatementCallback)}
     * or {@link DataTemplate#execute(PreparedStatementCreator, PreparedStatementCallback)}.
     * <p><b>ATTENTION: any ResultSet should be closed within this callback implementation.
     * This method doesn't imply that the ResultSet (as other resources) will be closed.
     * Still, this method should grant (as shown in DataTemplate various implementations) that the statement will be closed at the end of the operations.</b>
     * @param ps an active PreparedStatement
     * @return a result object or null if it's not available
     * @throws SQLException if thrown by a DataTemplate's method (no need to catch).
     */
    @Nullable
    T doInPreparedStatement(PreparedStatement ps) throws SQLException;

}
