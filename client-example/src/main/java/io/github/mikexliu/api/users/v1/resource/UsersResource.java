package io.github.mikexliu.api.users.v1.resource;

import io.github.mikexliu.collect.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

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

    @ApiOperation(value = "all", notes = "get all user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/all")
    public abstract Map<String, User> all();
}