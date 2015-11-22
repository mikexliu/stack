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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Source:
 * https://github.com/swagger-api/swagger-samples/blob/master/java/java-jersey-jaxrs/src/main/java/io/swagger/sample/resource/UserResource.java
 */
@Remote(endpoint = "http://petstore.swagger.io/v2/user")
@Path("/api/user")
@Api(value = "/user", description = "Operations about user")
public abstract class UserResource {

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
    public abstract String loginUser(
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