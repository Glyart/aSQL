# asyncSQL

An efficient library for dealing with databases asynchronously. Read [Javadoc](https://asql.glyart.com/) here.
A little request: while reading, use the Javadoc to understand in deep how these elements work.

## How to add this library in your project

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Glyart.aSQL</groupId>
        <artifactId>aSQL-<context></artifactId>
        <version>VERSION</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

# How to use?

This library is context-based. According to various definition we simply put that a Java context is an environment: it represents the state of the system you are using.
(Read more about contexts [here](https://stackoverflow.com/questions/3918083/what-exactly-is-a-context-in-java))

This library is based on 3 contexts:

- Spigot;
- Bungeecord;
- Velocity.

aSQL also has its own context, which is called [ASQLContext](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/context/ASQLContext.html). This context has various different implementations, depending on what context (the 3 above) you are using.

So you have to choose the module that represents the context you're going to use.

Let's assume we'll use the Spigot one (remember to change the artifactId into aSQL-spigot).

## Settings credentials

Just start by instantiating a [DataSourceCredentials](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/DataSourceCredentials.html) object.

```java
DataSourceCredentials credentials = DataSourceCredentials.builder()
  .setHostName("yourHostName")
  .setPort(3306) // Default port is 3306
  .setPassword("yourPassword")
  .setUsername("yourUsername")
  .setDatabase("yourDatabase")
  .build();
```

## Creating the context

Instantiate the right ASQLContext, which is [SpigotASQLContext](http://asql.glyart.com/aSQL-spigot/com/glyart/asql/spigot/SpigotASQLContext.html). Then open the connection pool by getting the [DataSourceHandler](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/DataSourceHandler.html).

```java
SpigotASQLContext context = SpigotASQLContext.builder()
  .setCredentials(credentials)
  .setPlugin(yourPluginInstance)
  .build();

try {
    context.getDataSourceHandler().open();
} catch (SQLException e) {
    // handle the exception the way you prefer
}

// Remember to close the connection pool when it's no more needed.
// Usually, this is done in onDisable method

@Override
public void onDisable() {
    try {
        context.getDataSourceHandler.close();
    catch (SQLException e) {
        // handle the exception the way you prefer
    }
}
```

## Custom DataSourceHandler

If you don't need to implement your version of DataSourceHandler just skip this part and go straight forward to the next one.

If you want to create your own DataSourceHandler you need to implement the DataSourceHandler interface:

```java
public class MyDataSourceHandler implements DataSourceHandler {
    
    // Constructors and other things

    @NotNull
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    @Override
    public void open() throws SQLException {

    }

    @Override
    public void close() throws SQLException {

    }

    @NotNull
    @Override
    public Strategy getStrategy() {
        return YOUR_STRATEGY;
    }
}

// In another part of your plugin
SpigotASQLContext context = SpigotASQLContext.builder()
  .setCredentials(credentials)
  .setPlugin(yourPluginInstance)
  .setDatabaseHandler(yourHandler)
  .build();

// Do the following if your strategy is CONNECTION_POOL
try {
    context.getDataSourceHandler().open();
} catch (SQLException e) {
    // handle the exception the way you prefer
}
// Again, remember to close the connection pool when it's no more needed.
```
Read more about [strategies](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/Strategy.html).

## Data access operations

Finally we are ready to use the [DataTemplate](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/DataTemplate.html) class to access the database. You can get it's instance by doing:

```java
DataTemplate<SpigotASQLContext> dataTemplate = context.getDataTemplate();
```

Note: *the DataTemplate class, its methods, and the [interfaces](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/functions/package-summary.html) used in their parameters as well, follow the same logic as Spring JDBC. We simplified it and only took the very essentials elements: the ones useful to a plugin developer for Minecraft servers. After that, we adapted them to be used in a plugin for Minecraft servers.*
[Learn more about Spring JDBC](https://docs.spring.io/spring-framework/docs/4.0.x/spring-framework-reference/html/jdbc.html).

The DataTemplate [has a lot of methods](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/DataTemplate.html) which can perform data access operations given callback objects.
Anyway, you don't need to do such complicated things: these methods are heavily overloaded. 
Each overload gives different combination of parameters, until we get methods which do not need callback objects, because default callback implementations are already provided internally.

This section will only show examples on methods whose parameters take:

- SQL statements (static or with "?" placeholders);
- Array of objects representing the SQL statement's parameters (needed with parametrized SQL statements only);
- Lambda functions (RowMapper) which holds a mapping logic for supplying results (query methods only);
- Two other interfaces used for batch updates. They will be discussed in the batch updates section.

Remember that every data access method returns a [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) object. You must invoke [whenComplete](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html#whenComplete-java.util.function.BiConsumer-) method when you're going to access the result(s).

Let's assume we are working with a simple table:

![https://i.imgur.com/AFatpsY.png](https://i.imgur.com/AFatpsY.png)

We need to represents this table by using a Java class, but this is simple either:

```java
public class User {

    private int id;
    private String name;
    private int score;

    public User(int id, String name) {
        this.id = id;
        this.name = name;	
    }

    // getters, setter, constructor(s)
}
```

### Query

If you need to query the database you can use two methods: queryForList and queryForObject.

The first one gets a list of results, the second one gets one result. Use it when you are sure that the query will supply exactly one result. Read more about this in the [Javadoc](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/database/DataTemplate.html).

Query methods need a RowMapper implementation. A RowMapper implementation maps a result for each ResultSet row ( we don't worry about exceptions or empty ResultSets). Read more about this in the [Javadoc](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/functions/RowMapper.html).

Example on getting a list by using a static SQL statement:

```java
CompletableFuture<List<User>> future = dataTemplate.queryForList("SELECT * FROM users", (resultSet, rowNumber) -> {
    /* We use this RowMapper implementation to work with ResultSet's rows.
    *  For example, if we want to get users with 0 score only we can do the following:
    */
    if (resultSet.getInt("score") == 0) {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        return user;
    }
});

// Just wait for the query to complete. When it's time, whenComplete method is executed
future.whenComplete((users, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // "users" is the list of results, extracted from ResultSet with RowMapper (users with 0 score)
    // note that the list can be empty, but never null
    for (User user : users) 
        player.sendMessage(user.getId() + " - " + user.getName());
});
```

Example on getting a single result by using a SQL statement with single parameter:

```java
String sql = "SELECT * FROM users WHERE id = ?";
CompletableFuture<User> future = dataTemplate.queryForObject(sql, new Integer[] {1}, (resultSet, rowNumber) -> {
    // Code inside this lambda will be executed once
    return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3));
});

// Same logic as before
future.whenComplete((user, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // Warning: a single result can be null
    if (user != null)
        player.sendMessage("Score of " + user.getName() + ": " + user.getScore());
});
```

Example on getting a single result by using a SQL statement with multiple parameters:

```java
String sql = "SELECT * FROM users WHERE id = ? OR score > ?"
// If parameter types are different we must use new Object[] {...}
// e.g. new Object[] {1, "HelloThirdParam", 4.4, otherRandomVariable}
CompletableFuture<User> future = dataTemplate.queryForObject(sql, new Integer[] {1, 10}, (resultSet, rowNumber) -> {
    return new User(resultSet.getInt(1), resultSet.getString(2));
});

// Same logic as before
future.whenComplete((user, exception) -> {
    if (exception != null) {
        // you can handle the error
        return;
    }
    // Warning: a single result can be null
    if (user != null) {
        // things
    }
});
```

Right now, supported parameter types are:

- boolean;
- integer;
- float;
- long;
- big integer;
- byte;
- short;
- double;
- big decimal;
- string;
- UUID;
- these Java SQL wrappers: Date, Time, Timestamp.

### Single update (delete, insert, update, create, drop...)

These methods can handle every type of update statement (static or not).

Every update method returns the number of the affected rows. By setting `getGeneratedKeys` argument on true, the method will return the primary key of the generated row (if it was really created).
Note: *right now, this works with numeric primary keys only. "getGeneratedKeys" is useless when you are not using an INSERT statement.*

The usage of these methods is as simple as the query ones. Here's some examples.

Update with parametrized SQL statement:

```java
String sql = "INSERT INTO users VALUES(?, ?, ?)";
CompletableFuture<Integer> future = dataTemplate.update(sql, new Object[] {3, "ErMandarone", 10}, false);

// Same logic as before
future.whenComplete((integer, exception) -> {
    if (exception != null) {
      return; // you can handle the error
    }
    System.out.println(integer); // Expected 1
}
```

Update with static SQL statement:

```java
String sql = "INSERT INTO users VALUES(null, 'Helo', 50)";
CompletableFuture<Integer> future = dataTemplate.update(sql, true);

// Same logic as before
future.whenComplete((integer, exception) -> {
    if (exception != null) {
      // you can handle the error
      return;
    }
    System.out.println(integer); // Expected the primary key of this new row
}
```

### Batch update (delete, insert, update, create, drop...)

These methods performs multiple updates by using the same SQL statement.

Right now, no results are supplied by DataTemplate's batch update methods. Anyway, you can handle possible exceptions.

Usage of these interfaces is encouraged when you are using these methods:

- [BatchPreparedStatementSetter](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/functions/BatchPreparedStatementSetter.html);
- [ParametrizedPreparedStatementSetter](http://asql.glyart.com/aSQL-common/com/glyart/asql/common/functions/ParametrizedPreparedStatementSetter.html).

Read their documentations for further information.

Example with BatchPreparedStatementSetter:

```java
// Let's prepare 100 insert statement
List<User> users = new ArrayList<>();
for (int i = 0; i < 100; i++) 
    users.add(new User(i, "Test" + 1, 0));

String sql = "INSERT INTO users VALUES(?, ?, ?)";

CompletableFuture<Void> future = dataTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
    @Override
    public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
	User user = users.get(i);
        ps.setInt(1, user.getId());
	ps.setString(2, user.getName());
	ps.setInt(3, 0); 
    }

    @Override
    public int getBatchSize() {
        return users.size();
    }
});

//Same logic as before
future.whenComplete((unused, exception) -> {
    if (exception != null) {
    // you can handle the error
    }
});

```

Example with ParametrizedPreparedStatementSetter:

```java
// Assume that "users" is a list containing 100 different users.

String sql = "INSERT INTO users VALUES(?, ?, ?)";

CompletableFuture<Void> future = dataTemplate.batchUpdate(sql, users, (preparedStatement, user) -> {
    ps.setInt(1, user.getId());
    ps.setString(2, user.getName());
    ps.setInt(3, user.getScore());
});

//Same logic as before
future.whenComplete((unused, exception) -> {
    if (exception != null) {
    // you can handle the error
    }
});

```

## Custom callback implementations

Work in progress...
