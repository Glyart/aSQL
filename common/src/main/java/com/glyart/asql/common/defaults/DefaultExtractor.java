package com.glyart.asql.common.defaults;

import com.glyart.asql.common.functions.ResultSetExtractor;
import com.glyart.asql.common.functions.RowMapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DefaultExtractor<T> implements ResultSetExtractor<List<T>> {

    @NotNull
    private final RowMapper<T> mapper;
    private final int limit;

    public DefaultExtractor(@NotNull RowMapper<T> mapper) {
        this(mapper, 0);
    }

    public DefaultExtractor(@NotNull RowMapper<T> mapper, int limit) {
        Preconditions.checkNotNull(mapper, "RowMapper cannot be null.");
        this.mapper = mapper;
        this.limit = limit;
    }

    @Nullable
    @Override
    public List<T> extractData(@NotNull ResultSet rs) throws SQLException {
        List<T> list = limit == 0 ? new ArrayList<>() : new ArrayList<>(limit);
        int rowNum = 0;
        while (rs.next())
            list.add(mapper.map(rs, rowNum++));

        return list;
    }
}