#stack
##Introduction
Build your own REST endpoint right out of the box without worrying about how it's wired together. Takes advantage of the following libraries (only `guice` and `jersey` are required):
* `guice` dependency injection
* `jersey` rest annotations
* `jetty` simple but powerful webserver that is pre-configured to work with `swagger`
* `swagger-ui` self document your apis and have a clean interface to using them

##Usage
There are only three classes (and one main class) that need to be implemented to see everything in action:

Define the `resource`. The class should be `abstract` as only the `annotations` are used. It follows the `jax-rs` specifications.
```java
package example.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import example.container.MyItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "my-resource")
@Path("/api/my-resource")
public abstract class MyResource {

    @ApiOperation(value = "create", notes = "Creates and returns the id of a JSON representation of MyItem.")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public abstract String create(final MyItem item);

    @ApiOperation(value = "read", notes = "Returns a JSON representation of the specified MyItem.")
    @GET
    @Path("/{_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract MyItem read(@PathParam("_id") final String _id);

    @ApiOperation(value = "update", notes = "Updates the specified MyItem with a new JSON representation.")
    @PUT
    @Path("/{_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response update(@PathParam("_id") final String _id, final MyItem item);

    @ApiOperation(value = "delete", notes = "Deletes the specified MyItem.")
    @DELETE
    @Path("/{_id}")
    public abstract Response delete(@PathParam("_id") final String _id);
}
```

Define the `container`. This is where the `application` lies.
```java
package example.container;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Maps;

import example.resource.MyResource;

public final class MyContainer extends MyResource {

    private Map<String, MyItem> items = Maps.newHashMap();

    /**
     * Create an MyItem object
     */
    @Override
    public String create(final MyItem item) {
        item._id = UUID.randomUUID().toString();
        items.put(item._id, item);
        return item._id;
    }

    /**
     * If the item to update does not exist, returns 204
     * Otherwise, returns the object
     * 
     * This shows we can return non-Response, non-String objects.
     */
    @Override
    public MyItem read(final String _id) {
        return items.get(_id);
    }

    /**
     * If the item to update does not exist, return 404
     */
    @Override
    public Response update(final String _id, final MyItem item) {
        if (!items.containsKey(_id)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        item._id = _id;
        items.put(_id, item);
        return Response.ok(item, MediaType.APPLICATION_JSON).build();
    }

    /**
     * This is successful even if no item exists
     */
    @Override
    public Response delete(final String _id) {
        items.remove(_id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
```

Define the `module`. This is the piece that glues the `resource` to the `container`.
```java
package example.container;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import example.resource.MyResource;
import inject.StackModule;
import web.Stack.ResponseThrowableHandler;

public class MyModule extends StackModule {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);

        bind(ResponseThrowableHandler.class).toInstance(new ResponseThrowableHandler() {

            @Override
            public Response handleThrowable(final Throwable throwable) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).build();
            }
        });
    }
}
```

Finally, start up the `application`.
```java
package example;

import com.google.inject.Guice;
import com.google.inject.Injector;

import example.container.MyModule;
import web.Stack;

public class Main {

    public static void main(String[] args) {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        new Stack(injector).start();
    }
}
```

Let's see it in action:
```
user@vm:~$ curl -X POST "http://localhost:5555/api/my-resource/" -d "{\"data\":\"data\"}" --header 'Content-Type: application/json'
1f1dd3af-2a6c-4b54-bcf6-f125d3fada65
```
```
user@vm:~$ curl -X GET "http://localhost:5555/api/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65"
{"_id":"1f1dd3af-2a6c-4b54-bcf6-f125d3fada65","data":"data"}
```
```
curl -X PUT "http://localhost:5555/api/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65" -d "{\"data\": \"data2\"}" --header 'Content-Type: application/json'
{"_id":"1f1dd3af-2a6c-4b54-bcf6-f125d3fada65","data":"data2"}
```
```
curl -X DELETE "http://localhost:5555/api/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65"
curl -X GET "http://localhost:5555/api/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65" -I
HTTP/1.1 204 No Content
Content-Type: application/json
Date: Tue, 29 Sep 2015 05:43:26 GMT
```

##Restrictions
* The resource's package must be separate from the container's package.
* The container must have a default constructor. This means to use `guice`, fields must be at least package private.

##Swagger
If you use the built-in Stack class to start up the application, then we can also take advantage of `swagger`. Navigate to `http://localhost:5555/swagger.json/` to see the swagger representation. `swagger-ui` is also already configured for you at `http://localhost:5555/docs`.

##Example
`git clone https://github.com/mikexliu/stack.git`

`mvn install; mvn exec:java`

`http://localhost:5555/docs` should now be accessible with all resources defined.

#Extra Goodies
* `server` ready for use (not optimized)
* `swagger` ready for use (not optimized)
* `persistence` not implemented
* `authentication` not implemented

#Future
* separate resource paths from swagger paths
* auto find resources
* auto filter guice resources better
