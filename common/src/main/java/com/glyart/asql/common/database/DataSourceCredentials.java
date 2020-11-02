package com.glyart.asql.common.database;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

/**
 * Represents the credentials for a connection to a data source.
 * Multiple different connections can be established by building other DataSourceCredentials instances.
 * @see DataSourceCredentialsBuilder
 */
@SuppressWarnings("unused")
public class DataSourceCredentials {

    @Nullable
    private Properties properties;

    @NotNull
    private final String hostname;

    private int port;

    @NotNull
    private final String username;

    @Nullable
    private String password;

    @Nullable
    private String database;

    /**
     * Construct a {@link DataSourceCredentials} object by a given properties object. <br>
     * This properties object can contains all the allowed configurations supported by Hikari. <br>
     * More details <a href="https://github.com/brettwooldridge/HikariCP">here</a>.
     * @param properties The ready properties for connecting to a data source
     */
    public DataSourceCredentials(@NotNull Properties properties) {
        Preconditions.checkNotNull(properties, "Properties cannot be null.");
        Preconditions.checkArgument(!properties.isEmpty(), "Properties must not be blank.");
        hostname = Preconditions.checkNotNull(properties.getProperty("hostname"), "hostname field is missing in properties.");
        port = Integer.parseInt((String) properties.getOrDefault("port", 3306));
        username = Preconditions.checkNotNull(properties.getProperty("username"), "username field is missing in properties.");
        password = properties.getProperty("password");
        database = properties.getProperty("database");
        this.properties = properties;
    }

    private DataSourceCredentials(@NotNull String hostname, @NotNull String username) {
        this.hostname = hostname;
        this.username = username;
    }

    public static DataSourceCredentialsBuilder builder() {
        return new DataSourceCredentialsBuilder();
    }

    @Nullable
    public Properties getProperties() {
        return this.properties;
    }

    @NotNull
    public String getHostname() {
        return this.hostname;
    }

    public int getPort() {
        return this.port;
    }

    @NotNull
    public String getUsername() {
        return this.username;
    }

    @Nullable
    public String getPassword() {
        return this.password;
    }

    @Nullable
    public String getDatabase() {
        return this.database;
    }

    /**
     * Represents a builder for creating an instance of DataSourceCredential.
     */
    public static class DataSourceCredentialsBuilder {

        private String hostname;
        private int port;
        private String username;
        private String password;
        private String database;

        private DataSourceCredentialsBuilder() {

        }

        /**
         * Sets the hostname for connecting to a data source.
         * @param hostName The hostname of the physical data source
         * @return The instance of this builder
         */
        public DataSourceCredentialsBuilder setHostName(@NotNull String hostName) {
            Preconditions.checkArgument(!hostName.isEmpty(), "Hostname cannot be empty.");
            this.hostname = hostName;
            return this;
        }

        /**
         * Sets the port for connecting to a data source.
         * @param port The port of the physical data source
         * @return The instance of this builder
         */
        public DataSourceCredentialsBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the username for connecting to a data source.
         * @param username The username for the access
         * @return The instance of this builder
         */
        public DataSourceCredentialsBuilder setUsername(@NotNull String username) {
            Preconditions.checkArgument(!username.isEmpty(), "Username cannot be empty.");
            this.username = username;
            return this;
        }

        /**
         * Sets the password for connecting to a data source.
         * @param password The password for the access
         * @return The instance of this builder
         */
        public DataSourceCredentialsBuilder setPassword(@Nullable String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the initial schema to use after the connection to a data source is successfully established.<br>
         * <b>This will not provide the creation of the schema if it doesn't exist.</b>
         * @param schema An existing database schema
         * @return The instance of this builder
         */
        public DataSourceCredentialsBuilder setDatabase(@Nullable String schema) {
            this.database = schema;
            return this;
        }

        /**
         * Builds a {@link DataSourceCredentials} object, ready to be passed to a {@link com.glyart.asql.common.context.ASQLContext} implementation.
         * @return A new instance of DataSourceCredentials.
         */
        @NotNull
        public DataSourceCredentials build() {
            DataSourceCredentials dataSourceCredentials = new DataSourceCredentials(hostname, username);

            dataSourceCredentials.port = port;
            dataSourceCredentials.password = password;
            dataSourceCredentials.database = database;

            return dataSourceCredentials;
        }
    }
}
