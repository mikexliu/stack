# stack

# introduction
`stack` allows you to build a REST endpoint while truly separating the endpoint definitions and the implementation.

# quick start
## installation
* jdk8
* maven3
* guice
* jersey
* swagger-ui

## dependency
```xml
<dependency>
    <groupId>io.github.mikexliu</groupId>
    <artifactId>stack-server</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
```xml
<dependency>
    <groupId>io.github.mikexliu</groupId>
    <artifactId>stack-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## start coding
`stack` requires very little code to get started. The source code for the examples are available in the repository. 
**Some lines have been removed for clarity in this README. All sources are linked.**

### creating a server
Define an endpoint using standard [jersey](https://jersey.java.net/documentation/latest/jaxrs-resources.html) and [swagger](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X).
Note that the classes and all methods are abstract.
```java
package io.github.mikexliu.api.users.v1.resource;

@Api(value = "users api", description = "")
@Path("/api/users/v1")
public abstract class UsersResource {

    @ApiOperation(value = "upload", notes = "upload user")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public abstract String post(
            @ApiParam(value = "user", required = true)
            final User user);

    @ApiOperation(value = "get", notes = "get user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public abstract User get(
            @ApiParam(value = "id", required = true)
            @PathParam(value = "id")
            final String id);
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-client/src/main/java/io/github/mikexliu/api/users/v1/resource/UsersResource.java)

Define the implementation by extending the endpoint class. The example implementation has very little logic but could
have very well contained a full application.
```java
package io.github.mikexliu.api.users.v1.container;

public class UsersContainer extends UsersResource {

    private final UsersCache usersCache;

    @Inject
    public UsersContainer(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    public String post(final User user) {
        return usersCache.addUser(user);
    }

    @Override
    public User get(String id) {
        return usersCache.getUser(id);
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-server/src/main/java/io/github/mikexliu/api/users/v1/container/UsersContainer.java)

Let's start it up!
```java
package io.github.mikexliu.main;

public class Main {

    public static void main(String[] args) throws Exception {
        StackServer.builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withSwaggerUiDirectory("swagger-ui")
                .withSwaggerEnabled()
                .withModule(new ServerExampleModule())
                .withExceptionHandler(throwable ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .build())
                .withPort(5454)
                .build()
                .start();
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-server/src/main/java/io/github/mikexliu/main/Main.java)

That's it! The endpoint is now ready to be used.

```bash
curl -X POST --header "Content-Type: application/json" --header "Accept: text/plain" -d "{
  \"name\": \"username\",
  \"age\": 0
}" "http://localhost:5454/api/users/v1"

a06e81b
```

```bash
curl -X GET --header "Accept: application/json" "http://localhost:5454/api/users/v1/a06e81b"

{
  "name": "username",
  "age": 0
}
```

### creating a client
No one is actually going to use the endpoints with curl though. We want to use this natively.
So let's create another simple main class that depends only on the resource class defined above.
```java
package io.github.mikexliu.main;

public class Main {
    public static void main(String[] args) {
        final StackClient stackClient = new StackClient("http", "localhost", 5454);
        final UsersResource usersResource = stackClient.getClient(UsersResource.class);

        final User user = new User();
        user.name = "new user";
        user.age = 12;
        final String id = usersResource.post(user);

        final User returnedUser = usersResource.get(id);

        System.out.println(user.name.equals(returnedUser.name)); // true
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-client/src/main/java/io/github/mikexliu/main/Main.java)

Notice we don't depend on `UsersContainer` at all. `StackClient` infers the endpoint from `UsersResource` and builds 
the request for you.  It automatically serializes the arguments and deserializes the return value so you don't have to do any work.
Because `stack-server` and `stack-client` are separated from each other, there's no chance of circular dependencies.

### expanding server functionality
Let's make the server do some active work. Users age, so let's add that! Note that it uses a custom version of
`AbstractScheduledService` (not the [guava](https://github.com/google/guava) version).
```java
package io.github.mikexliu.scheduledservice;

public class AgingService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(AgingService.class);

    private final UsersCache usersCache;

    @Inject
    public AgingService(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(2, 1, TimeUnit.SECONDS);
    }

    @Timed
    @Override
    public void runOneIteration() {
        try {
            usersCache.getAllUsers().values().forEach(User::growUp);
        } catch (Exception e) {
            log.warn(getClass() + " failed", e);
        }
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-server/src/main/java/io/github/mikexliu/scheduledservice/AgingService.java)

You might have noticed the `@Timed` annotation. `stack` includes metrics reporting out of the box as well!
In this case, we want to track how long the execution takes. All of the metrics are added into [`dropwizard metrics`](https://dropwizard.github.io/metrics/3.1.0/getting-started/).
To access the service and metrics, we'll need to add two new lines to the `main` class:
```java
.withPlugin(ScheduledServicePlugin.class)
.withPlugin(TimedPlugin.class)
```

```java
package io.github.mikexliu.main;

public class Main {

    public static void main(String[] args) throws Exception {
        StackServer.builder()
                .withTitle("server-example")
                .withDescription("server-example description")
                .withVersion("0.0.1-SNAPSHOT")
                .withApiPackageName("io.github.mikexliu.api")
                .withSwaggerUiDirectory("swagger-ui")
                .withSwaggerEnabled()
                .withPlugin(ScheduledServicePlugin.class)
                .withPlugin(TimedPlugin.class)
                .withModule(new ServerExampleModule())
                .withCorsEnabled()
                .withExceptionHandler(throwable ->
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                .build())
                .withPort(5454)
                .build()
                .start();
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-server/src/main/java/io/github/mikexliu/main/Main.java)

The details are in another documentation, but with the inclusion of these two `plugins`, we've added
quite a few more endpoints we can use.

```bash
curl -X GET --header "Accept: application/json" "http://localhost:5454/api/stack/scheduled-services/v1/get-service-states"

{
  "AgingService": "RUNNING"
}
```

```bash
curl -X GET --header "Accept: application/json" "http://localhost:5454/api/stack/metrics/v1/get-metrics"

{
  "io.github.mikexliu.scheduledservice.AgingService.run.timer": {
    "min": "196659",
    "max": "2007820107",
    "mean": "8.827260014007807E8",
    "count": "763"
  }
}
```

This application is now fully ready to be used. It can store `User` data, manage `AgingService`,
automatically increment their ages on a timer, and report how long it takes to do so.

# documentation
## stack-server
`stack-server` creates the actual endpoint services. It also supports scheduled services and metrics out of the
box. Some more advanced services that might go here include caches, asynchronous work, or even streams.

### plugins
`stack-server` was made to be heavily customizable. For any feature that is deemed useful, a plugin is created instead
of hardcoding the feature in. A plugin is just a `module` in `guice`; however, it finds the dependencies and excutes code
before `stack-server` starts. This allows it to do pre-processing work such as scanning for bound classes or registering
more `resource` endpoints. Let's go over two examples plugins that work together: `timed` and `metrics`.
#### timed plugin
```java
package io.github.mikexliu.stack.guice.plugins.timed;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
    String key() default "";
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/timed/Timed.java)
```java
package io.github.mikexliu.stack.guice.plugins.timed;

public class TimedInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TimedInterceptor.class);

    @Inject
    private MetricRegistry metricRegistry;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Class<?> callingClass = getClass(invocation.getThis().getClass());
        final String methodName = invocation.getMethod().getName();
        final Timer timerMetric = metricRegistry.timer(MetricRegistry.name(callingClass, methodName, "timer"));
        final Timer.Context timerContext = timerMetric.time();
        try {
            log.info(String.format("%s.%s started", callingClass, methodName));
            return invocation.proceed();
        } finally {
            final long elapsed = TimeUnit.NANOSECONDS.toMillis(timerContext.stop());
            log.info(String.format("%s.%s finished; took %sms", callingClass, methodName, elapsed));
        }
    }

    private Class<?> getClass(final Class<?> callingClass) {
        if (callingClass.getSimpleName().contains("EnhancerByGuice")) {
            return callingClass.getSuperclass();
        }
        return callingClass;
    }
}

```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/timed/TimedInterceptor.java)
```java
package io.github.mikexliu.stack.guice.plugins.timed;

public class TimedPlugin extends StackPlugin {

    public TimedPlugin() {
        bindDependency(MetricsPlugin.class);
    }

    @Override
    protected void configure() {
        final TimedInterceptor interceptor = new TimedInterceptor();
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(Timed.class), interceptor);
        requestInjection(interceptor);
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/timed/TimedPlugin.java)
Notice the call `bindDependency`. This tells stack that we also need to include the `MetricsPlugin` as part of its
dependency injection algorithm. `timed` needs an instance of `MetricRegistry` in `TimedInterceptor` but we don't want to
create an instance for every `Timed` annotation. In fact, there should only be one instance so the metrics can be
extracted! Calling the `bindDependency` that only one instance is created and the `requestInjection` is successful.

Unfortunately, there is no way for `guice` to include an interceptor by supplying the `class` object. That is why we must
have `requestInjection`.

#### metrics plugin
```java
package io.github.mikexliu.stack.guice.plugins.metrics;

@Api(value = "Metrics manager api", description = "Metrics description")
@Path("/api/stack/metrics/v1")
public final class MetricsManagerResource {

    private static final Logger log = LoggerFactory.getLogger(MetricsManagerResource.class);

    private final MetricsManager metricsManager;

    @Inject
    public MetricsManagerResource(final MetricsManager metricsManager) {
        this.metricsManager = metricsManager;
    }

    @ApiOperation(value = "get-metrics",
            notes = "Returns all available metrics")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/get-metrics")
    public Map<String, Map<String, String>> getServices() {
        return metricsManager.getMetrics();
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/metrics/MetricsManagerResource.java)
```java
package io.github.mikexliu.stack.guice.plugins.metrics;

public class MetricsManager {

    private static final Logger log = LoggerFactory.getLogger(MetricsManager.class);

    private final MetricRegistry metricRegistry;

    @Inject
    public MetricsManager(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Map<String, Map<String, String>> getMetrics() {
        final Map<String, Map<String, String>> metrics = new HashMap<>();

        metricRegistry.getTimers().entrySet().forEach(entry -> {
            final Map<String, String> data = new HashMap<>();
            final Timer timerMetric = entry.getValue();
            final Snapshot snapshot = entry.getValue().getSnapshot();
            data.put("count", Long.toString(timerMetric.getCount()));
            data.put("min", Long.toString(snapshot.getMin()));
            data.put("max", Long.toString(snapshot.getMax()));
            data.put("mean", Double.toString(snapshot.getMean()));

            metrics.put(entry.getKey(), data);
        });
        return metrics;
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/metrics/MetricsManager.java)
```java
package io.github.mikexliu.stack.guice.plugins.metrics;

public class MetricsPlugin extends StackPlugin {

    @Override
    protected void configure() {
        bind(MetricsManagerResource.class).in(Scopes.SINGLETON);

        bind(MetricRegistry.class).in(Scopes.SINGLETON);
        bind(MetricsManager.class).in(Scopes.SINGLETON);
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/stack-server/src/main/java/io/github/mikexliu/stack/guice/plugins/metrics/MetricsPlugin.java)
Simply by including `MetricsManagerResource`, we create an entirely new endpoint that can be used by the server. Notice
it does not follow the convention `abstract class` and `implementing class`. This is purely because we do not expect
there to be multiple implementations of the `resource`.

## stack-client

### native code
```TODO: not implemented```

### remote client
If an existing REST endpoint exists with valid `jersey` annotations defined, then we can use that code and create a client immediately.

This user resource comes directly from the [swagger-ui test page](http://petstore.swagger.io).
[External source code](https://github.com/swagger-api/swagger-samples/blob/master/java/java-jersey-jaxrs/src/main/java/io/swagger/sample/resource/UserResource.java).
```java
package io.github.mikexliu.api.petstore.v2.user;

@Path("/v2/user")
@Api(value = "/user", description = "Operations about user")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public abstract class UserResource {

    @POST
    @ApiOperation(value = "Create user",
            notes = "This can only be done by the logged in user.",
            position = 1)
    public abstract void createUser(
            @ApiParam(value = "Created user object", required = true)
            final User user);
            
    @PUT
        @Path("/{username}")
        @ApiOperation(value = "Updated user",
                notes = "This can only be done by the logged in user.",
                position = 4)
        @ApiResponses(value = {
                @ApiResponse(code = 400, message = "Invalid user supplied"),
                @ApiResponse(code = 404, message = "User not found")})
        public abstract void updateUser(
                @ApiParam(value = "name that need to be updated", required = true)
                @PathParam("username")
                final String username,
    
                @ApiParam(value = "Updated user object", required = true)
                final User user);

    @GET
    @Path("/{username}")
    @ApiOperation(value = "Get user by user name",
            response = User.class,
            position = 0)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid username supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    @Produces(MediaType.APPLICATION_JSON)
    public abstract User getUserByName(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true)
            @PathParam("username")
            final String username);
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-remote/src/main/java/io/github/mikexliu/api/petstore/v2/user/UserResource.java)

In this example, we make a remote call against an [actual endpoint](http://petstore.swagger.io) that we have no control over.
The code creates and updates a user. In between each step, we verify against the server that the data is correct.
```java
package io.github.mikexliu.main;

public class Main {

    public static void main(String[] args) {
        final StackClient stackClient = new StackClient("http", "petstore.swagger.io", 80);
        final UserResource userResource = stackClient.getClient(UserResource.class);

        final User user = new User();
        user.id = 1234;
        user.firstName = "first name";
        user.lastName = "last name";
        user.email = "mxl@github.io";
        user.phone = "123-456-7890";
        user.username = "mxl";
        user.password = "hi";
        user.userStatus = 5;
        userResource.createUser(user);

        User response = userResource.getUserByName("mxl");
        System.out.println(user.firstName.equals(response.firstName)); // true

        user.firstName = "changed name";
        response = userResource.getUserByName("mxl");
        System.out.println(user.firstName.equals(response.firstName)); // false

        userResource.updateUser("mxl", user);
        response = userResource.getUserByName("mxl");
        System.out.println(user.firstName.equals(response.firstName)); // true
    }
}
```
###### [source](https://github.com/mikexliu/stack/blob/master/example-remote/src/main/java/io/github/mikexliu/main/Main.java)

# planned (in no particular order)
- [x] metrics integration
- [ ] pagination plugin
- [ ] metric-ui plugin
- [ ] swagger-ui jar extension
- [ ] javadoc generation
- [ ] authentication

# license
    Copyright 2016 Mike Liu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
