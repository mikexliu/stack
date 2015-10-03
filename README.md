#stack
##Introduction
Provide an extremely easy-to-setup REST endpoint. Many libraries stress separation of concerns and modular code but very few end up actually being implemented correctly. With `stack`, not only will your project be modular, it will come naturally with Java's coding style.

##Dependencies
* javassist 3.12.1.GA
* jackson-jaxrs-json-provider 2.6.2
* jersey-json 1.19
* jersey-guice 1.19
* jersey-grizzly2 1.19
* servlet-api 2.5
* guice 3.0
* guava 18.0
* junit 4.12

##Usage
There are only four classes (and one main class) that need to be implemented to see everything in action:

Define the `interface`. This is the interface that is shared between the `resource` and the `container`.
```java
import javax.ws.rs.core.Response;

public interface MyInterface {

    public String create(final MyItem item);

    public MyItem read(final String _id);

    public Response update(final String _id, final MyItem item);

    public Response delete(final String _id);
}
```

Define the `resource`. The class should be `abstract` as only the `annotations` are used. It follows the `jax-rs` specifications and implements the `interface` defined.
```java
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

@Path("/my-resource")
public abstract class MyResource implements MyInterface {

    @Override
    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract String create(final MyItem item);

    @Override
    @GET
    @Path("/{_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract MyItem read(@PathParam("_id") final String _id);

    @Override
    @PUT
    @Path("/{_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response update(@PathParam("_id") final String _id, final MyItem item);

    @Override
    @DELETE
    @Path("/{_id}")
    public abstract Response delete(@PathParam("_id") final String _id);
}

```

Define the `container`. This is where the `application` lies. It also implements the `interface` defined.
```java
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Maps;

public final class MyContainer implements MyInterface {

    private Map<String, MyItem> items = Maps.newHashMap();

    @Override
    public String create(final MyItem item) {
        item._id = UUID.randomUUID().toString();
        items.put(item._id, item);
        return item._id;
    }

    @Override
    public MyItem read(final String _id) {
        return items.get(_id);
    }

    @Override
    public Response update(final String _id, final MyItem item) {
        if (!items.containsKey(_id)) {
            return Response.status(Status.NO_CONTENT).build();
        } else {
            item._id = _id;
            items.put(_id, item);
            return Response.ok().build();
        }
    }

    @Override
    public Response delete(final String _id) {
        items.remove(_id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
```

Define the `module`. This is the piece that glues the `resource` to the `container`.
```java
import inject.Module;

import com.google.inject.servlet.ServletModule;

public class MyModule extends Module<MyResource, MyContainer> {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);

        install(new ServletModule());
    }
}
```

Finally, start up the `application`.
```java
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.glassfish.grizzly.http.server.HttpServer;

import web.ServerBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
    public static void main(String[] args) throws IllegalArgumentException, IOException, URISyntaxException {

        final MyModule myModule = new MyModule();
        final Injector injector = Guice.createInjector(myModule);

        final HttpServer server = new ServerBuilder()
                .withEndpoint(new URI("http://localhost:5555"))
                .withInjector(injector)
                .build();

        server.start();
        System.out.println("Press any key to exit...");
        System.in.read();
    }
}
```

Let's see it in action:
```
user@vm:~$ curl -X POST "http://localhost:5555/my-resource/" -d "{\"data\":\"data\"}" --header 'Content-Type: application/json'
1f1dd3af-2a6c-4b54-bcf6-f125d3fada65
```
```
user@vm:~$ curl -X GET "http://localhost:5555/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65"
{"_id":"1f1dd3af-2a6c-4b54-bcf6-f125d3fada65","data":"data"}
```
```
curl -X PUT "http://localhost:5555/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65" -d "{\"data\": \"data2\"}" --header 'Content-Type: application/json'
{"_id":"1f1dd3af-2a6c-4b54-bcf6-f125d3fada65","data":"data2"}
```
```
curl -X DELETE "http://localhost:5555/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65"
curl -X GET "http://localhost:5555/my-resource/1f1dd3af-2a6c-4b54-bcf6-f125d3fada65" -I
HTTP/1.1 204 No Content
Content-Type: application/json
Date: Tue, 29 Sep 2015 05:43:26 GMT
```

#Extra Goodies
* `server` ready for use (not optimized)
* `swagger` not implemented
* `persistence` not implemented
