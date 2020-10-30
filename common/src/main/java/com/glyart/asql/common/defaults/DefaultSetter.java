package com.glyart.asql.common.defaults;

import com.glyart.asql.common.functions.PreparedStatementSetter;
import com.glyart.asql.common.utils.Utils;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultSetter implements PreparedStatementSetter {

    @Nullable
    private final Object[] params;

    @Override
    public void setValues(@NotNull PreparedStatement ps) throws SQLException {
        Preconditions.checkNotNull(ps, "PreparedStatement cannot be null.");
        if (params == null)
            return;

        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int sqlType = getSQlType(param);
            if (sqlType != Types.OTHER) {
                ps.setObject(i + 1, param, sqlType);
                continue;
            }
            // Particular cases go here
            if (param instanceof String || param instanceof UUID)
                ps.setString(i + 1, param.toString());
        }
    }

    /**
     * Gets the related type of a given parameter.
     * Refers to sql {@link Types} codes.
     * @param param The query parameter
     * @return The type of the given parameter
     */
    protected int getSQlType(Object param) {
        return Utils.getSqlType(param);
    }

}
