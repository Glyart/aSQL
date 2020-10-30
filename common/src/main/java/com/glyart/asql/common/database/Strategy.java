package com.glyart.asql.common.database;

/**
 * Describes the behavior of a {@link DataSourceHandler} implementation.
 * @see DataTemplate
 * @see DataSourceHandler
 */
public enum Strategy {

    /**
     * Represents that this DataSourceHandler implementation will be simple-connection based.
     * <p><b>NOTE: There is no need to manually call {@link DataSourceHandler#open()} and {@link DataSourceHandler#close()} methods.
     * The connection will be opened and closed respectively at the beginning and at the ending of a data source CRUD operation, by
     * the DataTemplate class.</b></p>
     */
    SIMPLE_CONNECTION,

    /**
     * Represents that this DataSourceHandler implementation will be connection-pool based.
     * <p><b>NOTE: {@link DataSourceHandler#open()} and {@link DataSourceHandler#close()} must be called manually.
     * aSQL has no way of knowing when it's time to open or close the connection pool.</b></p>
     */
    CONNECTION_POOL

}
