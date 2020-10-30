package com.glyart.asql.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Convenient class for shared (and rewritable) behaviors among implementations.
 * @see com.glyart.asql.common.defaults.DefaultBatchSetter
 * @see com.glyart.asql.common.defaults.DefaultSetter
 */
public final class Utils {

    private Utils() {

    }

    /**
     * Gets the sql type by a given object.
     * @param object The object
     * @return The type of the object, referring to {@link java.sql.SQLType} types list
     */
    public static int getSqlType(Object object) {
        if (object instanceof Boolean)
            return Types.BOOLEAN;

        else if (object instanceof Integer)
            return Types.INTEGER;

        else if (object instanceof Float)
            return Types.FLOAT;

        else if (object instanceof Long || object instanceof BigInteger)
            return Types.BIGINT;

        else if (object instanceof Byte)
            return Types.TINYINT;

        else if (object instanceof Short)
            return Types.SMALLINT;

        else if (object instanceof Double)
            return Types.DOUBLE;

        else if (object instanceof BigDecimal)
            return Types.DECIMAL;

        else if (object instanceof Date)
            return Types.DATE;

        else if (object instanceof Time)
            return Types.TIME;

        else if (object instanceof Timestamp)
            return Types.TIMESTAMP;

        return Types.OTHER;
    }

}
