package com.glyart.asql.common.functions;

import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.defaults.DefaultExtractor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a callback interface used by {@link DataTemplate}'s query methods.
 *
 * <p>Implementations of this interface extract results from a {@link ResultSet} and they
 * don't need to worry about handling exceptions: they will be handled by aSQL DataTemplate and passed to a CompletableFuture for further analysis by the user.</p>
 *
 * <p>This interface is internally used by DataTemplate and, like {@link RowMapper}, it's reusable.
 * A default implementation called DefaultExtractor is already provided.</p>
 * @param <T> the result type
 * @see DefaultExtractor
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    /**
     * Implementations of this method must provide the processing logic (data extraction) of the entire ResultSet.
     * @param rs the ResultSet to extract data from. Implementations don't need to close this: it will be closed by DataTemplate
     * @return an object result or null if it's not available
     * @throws SQLException if an SQLException is encountered while trying to navigate the ResultSet
     */
    @Nullable
    T extractData(@NotNull ResultSet rs) throws SQLException;

}
