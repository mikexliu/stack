package io.github.mikexliu.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Api(value = "users api", description = "")
@Path("/api/users/v1")
public abstract class UsersResource {

    @ApiOperation(value = "upload", notes = "post user")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public abstract String post(
            @ApiParam(value = "user", required = true)
            @QueryParam(value = "user")
            final String user);

    @ApiOperation(value = "get", notes = "get user")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public abstract String get(
            @ApiParam(value = "id", required = true)
            @PathParam(value = "id")
            final String id);
}