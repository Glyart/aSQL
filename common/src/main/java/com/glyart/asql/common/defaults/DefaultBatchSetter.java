package com.glyart.asql.common.defaults;

import com.glyart.asql.common.functions.BatchPreparedStatementSetter;
import com.glyart.asql.common.utils.Utils;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

public class DefaultBatchSetter implements BatchPreparedStatementSetter {

    private final List<Object[]> batchParams;

    public DefaultBatchSetter(@NotNull List<Object[]> batchParams) {
        this.batchParams = batchParams;
    }

    @Override
    public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
        Preconditions.checkNotNull(ps, "PreparedStatement cannot be null.");
        Object[] objects = batchParams.get(i);
        for (int pos = 0; pos < objects.length; pos++) {
            Object object = objects[pos];
            int sqlType = getSqlType(object);
            if (sqlType != Types.OTHER) {
                ps.setObject(pos + 1, object, sqlType);
                continue;
            }

            if (object instanceof String || object instanceof UUID) {
                ps.setString(pos + 1, object.toString());
            }
        }
    }

    @Override
    public int getBatchSize() {
        return batchParams.size();
    }

    /**
     * Gets the related type of a given parameter.
     * Refers to sql {@link Types} codes.
     * @param param The query parameter
     * @return The type of the given parameter
     */
    protected int getSqlType(Object param) {
        return Utils.getSqlType(param);
    }

}
