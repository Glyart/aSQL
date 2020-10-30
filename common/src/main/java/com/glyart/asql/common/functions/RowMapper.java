package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents an interface used to map rows of a {@link ResultSet}.
 *
 * <p>Implementations map each row to a generic result and they don't need to worry
 * about handling exceptions: they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 *
 * <p>This interface can be used for query methods or for other custom implementations.</p><br>
 * A RowMapper object is reusable. Also, it's a convenient and fast way for implementing a row-mapping logic in a single spot.
 * @param <T> The result type
 * @see DataTemplate#queryForList(String, RowMapper)
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Implementations will tell how to map EACH row of the ResultSet.<br>
     * {@link ResultSet#next()} call is not needed: this method should only map values of the current row.
     * @param rs the ResultSet, already initialized
     * @param rowNumber the number of the current row
     * @return a result object for the current row, or null if the result is not available
     * @throws SQLException if the implementation's trying to get column values in the wrong way
     */
    @Nullable
    T map(@NotNull ResultSet rs, int rowNumber) throws SQLException;

}
