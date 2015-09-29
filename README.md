#stack
##Introduction
Provide an extremely easy-to-setup REST endpoint. Many libraries stress separation of concerns and modular code but very few end up actually being implemented correctly. With `stack`, not only will your project be modular, it will come naturally with Java's coding style.

##Dependencies
* javassist: 3.12.1.GA
* guice: 3.0
* jersey-guice: 1.19
* jersey-core: 1.19
* jersey-grizzly2: 1.19
* javax.ws.rs-api: 2.0.1
* servlet-api: 2.5
* guava: 18.0
* junit: 4.12 [unused]

##Usage
There are only four classes (and one main class) that need to be implemented to see everything in action:

Define the `interface`. This is the interface that is shared between the `resource` and the `container`.
```java
public interface MyInterface {
    public String create(final MyItem item);

    public MyItem read(final String _id);

    public void update(final String _id, final MyItem item);

    public void delete(final String _id);
}

```

Define the `resource`. The class should be `abstract` as only the `annotations` are used. It follows the `jax-rs` specifications and implements the `interface` defined. Note that it must extend `Resource`.
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

import resources.Resource;

@Path("/my-resource")
public abstract class MyResource extends Resource implements MyInterface {

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
    public abstract void update(@PathParam("_id") final String _id, final MyItem item);

    @Override
    @DELETE
    @Path("/{_id}")
    public abstract void delete(@PathParam("_id") final String _id);
}

```

Define the `container`. This is where the `application` lies. It also implements the `interface` defined. Note that it must extended `Container`.
```java
import java.util.Map;
import java.util.UUID;

import resources.Container;

import com.google.common.collect.Maps;

public final class MyContainer extends Container implements MyInterface {

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
    public void update(final String _id, final MyItem item) {
        item._id = _id;
        items.put(item._id, item);
    }

    @Override
    public void delete(final String _id) {
        items.remove(_id);
    }
}
```

Define the `module`. This is the piece that glues the `resource` to the `container`.
```java
import inject.Module;
import web.SwaggerModule;

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
user@vm:~$ curl -X GET "http://localhost:5555/my-resource/1
```
```
Sep 28, 2015 8:40:42 PM com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory register
INFO: Registering example.MyResource as a root resource class
Sep 28, 2015 8:40:42 PM com.sun.jersey.server.impl.application.WebApplicationImpl _initiate
INFO: Initiating Jersey application, version 'Jersey: 1.19 02/11/2015 03:25 AM'
Sep 28, 2015 8:40:43 PM com.sun.jersey.guice.spi.container.GuiceComponentProviderFactory getComponentProvider
INFO: Binding example.MyResource to GuiceManagedComponentProvider with the scope "Singleton"
Sep 28, 2015 8:40:43 PM com.sun.jersey.spi.inject.Errors processErrorMessages
WARNING: The following warnings have been detected with resource and/or provider classes:
  WARNING: A sub-resource method, public abstract void example.MyResource.create(example.MyItem), with URI template, "/", is treated as a resource method
Sep 28, 2015 8:40:43 PM org.glassfish.grizzly.http.server.NetworkListener start
INFO: Started listener bound to [localhost:5555]
Sep 28, 2015 8:40:43 PM org.glassfish.grizzly.http.server.HttpServer start
INFO: [HttpServer] Started.
Press any key to exit...
read 1
```

#Extra Goodies
* `server` ready for use (not optimized)
* `swagger` not implemented
* `persistence` not implemented