#stack
##Introduction
Build your own REST endpoint right out of the box without worrying about how it's wired together. The following libraries are used automatically:
* `guice` dependency injection
* `jersey` rest annotations
* `swagger-ui` describe your rest api
* `jetty` webserver that hosts `swagger-ui`

##Usage
There are two primary ways of using `stack`: local and remote.

###Local
There are only two classes (and one main class) that need to be implemented to see everything in action:

Define the `resource`. The class must be `abstract`. This is where the `jax-rs` and `swagger` specifications are.
```java
package example.resource.v1;

import example.data.MyItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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

@Api(value = "/v1/my-resource", description = "simple resources with path parameters")
@Path("/api/v1/my-resource")
public abstract class LocalResource {

    @ApiOperation(
            value = "create",
            notes = "Creates and returns the id of the created MyItem.")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public abstract String create(
            @ApiParam("item description")
            final MyItem item);

    @ApiOperation(
            value = "read",
            notes = "Returns a JSON representation of the specified MyItem.")
    @GET
    @Path("/{_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract MyItem read(
            @ApiParam("_id description")
            @PathParam("_id")
            final String _id);

    @ApiOperation(
            value = "update",
            notes = "Updates the specified MyItem with a new JSON representation.")
    @PUT
    @Path("/{_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response update(
            @ApiParam("_id description")
            @PathParam("_id")
            final String _id,

            @ApiParam("item description")
            final MyItem item);

    @ApiOperation(
            value = "delete",
            notes = "Deletes the specified MyItem.")
    @DELETE
    @Path("/{_id}")
    public abstract Response delete(
            @ApiParam("_id description")
            @PathParam("_id")
            final String _id);
}
```

Define the `container`. This class must extend the `resource` class. This is where the `business logic` is.
```java
package example.container.v1;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import example.data.MyItem;
import example.main.Main;
import example.resource.v1.LocalResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.UUID;

public class LocalContainer extends LocalResource {

    /**
     * This object is injected from the top-level injector in {@link Main}.
     */
    @Inject
    @Named("items")
    Map<String, MyItem> items;

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
     * <p>
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

Start the `application`.
```java
package example.main;

import example.helper.StackServerHelper;

public class Main {

    public static void main(String[] args) throws Exception {
        new StackServerHelper(5556, "example.container,example.resource").start();
    }
}
```

###Remote
Definfe the `resource`. Note it has an `@Remote` annotation. The example comes directly from
https://github.com/swagger-api/swagger-samples/blob/master/java/java-jersey-jaxrs/src/main/java/io/swagger/sample/resource/UserResource.java
```java
package example.resource.petstore;

import example.data.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import stack.annotations.Remote;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * This class has no corresponding implementation locally. The implementation is defined
 * by the @Remote annotation and is hosted elsewhere. This resource serves as a proxy to
 * the remote implementation and provides a swagger-ui for it locally.
 *
 * Source:
 * https://github.com/swagger-api/swagger-samples/blob/master/java/java-jersey-jaxrs/src/main/java/io/swagger/sample/resource/UserResource.java
 */
@Remote(endpoint = "http://petstore.swagger.io/v2/user")
@Path("/api/user")
@Api(value = "/user", description = "Operations about user")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public abstract class RemoteResource {

    @POST
    @ApiOperation(value = "Create user",
            notes = "This can only be done by the logged in user.",
            position = 1)
    public abstract Response createUser(
            @ApiParam(value = "Created user object", required = true)
            final User user);

    @POST
    @Path("/createWithArray")
    @ApiOperation(value = "Creates list of users with given input array",
            position = 2)
    public abstract Response createUsersWithArrayInput(
            @ApiParam(value = "List of user object", required = true)
            final User[] users);

    @POST
    @Path("/createWithList")
    @ApiOperation(value = "Creates list of users with given input array",
            position = 3)
    public abstract Response createUsersWithListInput(
            @ApiParam(value = "List of user object", required = true)
            final List<User> users);

    @PUT
    @Path("/{username}")
    @ApiOperation(value = "Updated user",
            notes = "This can only be done by the logged in user.",
            position = 4)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid user supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public abstract Response updateUser(
            @ApiParam(value = "name that need to be updated", required = true)
            @PathParam("username")
            final String username,

            @ApiParam(value = "Updated user object", required = true)
            final User user);

    @DELETE
    @Path("/{username}")
    @ApiOperation(value = "Delete user",
            notes = "This can only be done by the logged in user.",
            position = 5)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid username supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public abstract Response deleteUser(
            @ApiParam(value = "The name that needs to be deleted", required = true)
            @PathParam("username")
            final String username);

    @GET
    @Path("/{username}")
    @ApiOperation(value = "Get user by user name",
            response = User.class,
            position = 0)
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid username supplied"),
            @ApiResponse(code = 404, message = "User not found")})
    public abstract Response getUserByName(
            @ApiParam(value = "The name that needs to be fetched. Use user1 for testing. ", required = true)
            @PathParam("username")
            final String username);

    @GET
    @Path("/login")
    @ApiOperation(value = "Logs user into the system",
            response = String.class,
            position = 6,
            responseHeaders = {
                    @ResponseHeader(name = "X-Expires-After", description = "date in UTC when token expires", response = Date.class),
                    @ResponseHeader(name = "X-Rate-Limit", description = "calls per hour allowed by the user", response = Integer.class)
            })
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid username/password supplied")})
    @Produces(MediaType.APPLICATION_XML) // TODO: work around; how to resolve?
    public abstract Response loginUser(
            @ApiParam(value = "The user name for login", required = true)
            @QueryParam("username")
            final String username,

            @ApiParam(value = "The password for login in clear text", required = true)
            @QueryParam("password")
            final String password);

    @GET
    @Path("/logout")
    @ApiOperation(value = "Logs out current logged in user session",
            position = 7)
    public abstract Response logoutUser();
}
```


##Restrictions
* Requires jdk8.
* The `resource` package must be separate from the `container` package.
* The `container` must have a constructor with zero arguments. This means to use `@Inject`, fields must be at least package private.
* The `api.prefix` in the properties must match all `@Path` prefix. Example: if `api.prefix=api`, then all `@Path` must begin with `/api/`.

##Example
`git clone https://github.com/mikexliu/stack.git`

`mvn clean install exec:java`

`http://localhost:5556/docs` should now be accessible with all resources defined.

##Future
* simple persistence (local file store, blob store, efficiency is not concerned)
* authentication
* metrics generation (which library to use? configurable?)
* remove guice requirement (might be difficult..)

##Technical Goals
* proxy client class (in-progress)
  * auto generated
    * asynchronous clients
  * configurable timeouts
* auto pagination
  * lazy return map so we can get key/value without returning the entire map