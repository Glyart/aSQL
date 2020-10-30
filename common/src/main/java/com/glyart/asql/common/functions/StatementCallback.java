package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Represents a callback interface for SQL statements.
 * It can execute multiple operations on a single Statement.
 * @param <T> The result type
 * @see DataTemplate#execute(StatementCallback)
 */
public interface StatementCallback<T> {

    /**
     * Gets called by DataTemplate.execute. Implementations of this method should not worry about handling exceptions: they will be handled
     * by aSQL DataTemplate and passed to CompletableFuture for further analysis by the user.
     *
     * <p><b>ATTENTION: any ResultSet should be closed within this callback implementation. This method doesn't imply
     * that the ResultSet (as other resources) will be closed.
     * Still, this method should grant (as shown in DataTemplate various implementations) that the statement
     * will be closed at the end of the operations.</b></p>
     * @param statement an active statement
     * @return a result of the statement execution. Null if no results are available
     * @throws SQLException if thrown by a DataTemplate method. Then, it will be inserted into the CompletableFuture
     * @see DataTemplate#update(String, boolean)
     * @see DataTemplate#query(String, ResultSetExtractor)
     */
    T doInStatement(Statement statement) throws SQLException;

}
