package com.glyart.asql.common.context;

import com.glyart.asql.common.database.DataSourceHandler;
import com.glyart.asql.common.database.DataTemplate;
import com.glyart.asql.common.database.AsyncDataAccessExecutor;
import com.glyart.asql.common.database.SyncDataAccessExecutor;

import java.util.logging.Logger;

/**
 * Represents a generic ASQLContext. An implementation of ASQLContext represents the state of the system where this API is used.
 * @param <T> The plugin type related to this context
 */
public interface ASQLContext<T> {

    /**
     * Gets the scheduling policy linked to this ASQLContext.
     * @return A singleton IScheduler instance
     * @see ContextScheduler
     */
    ContextScheduler getScheduler();

    /**
     * Gets the {@link DataTemplate} instance linked to this ASQLContext.
     * @return the data template related to this context
     */
    DataTemplate<AsyncDataAccessExecutor> getAsyncDataTemplate();

    /**
     * Gets the {@link DataTemplate} instance linked to this ASQLContext.
     * @return the data template related to this context
     */
    DataTemplate<SyncDataAccessExecutor> getSyncDataTemplate();

    /**
     * Gets the data source interaction strategy linked to this ASQLContext.
     * @return The data source handler
     * @see DataSourceHandler
     */
    DataSourceHandler getDataSourceHandler();

    /**
     * Gets the plugin who created this ASQLContext.
     * @return the plugin related to this context
     */
    T getPlugin();

    /**
     * Gets the logger used by this ASQLContext.
     * @return the context logger
     */
    Logger getLogger();

}
