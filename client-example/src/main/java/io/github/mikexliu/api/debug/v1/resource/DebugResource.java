package io.github.mikexliu.api.debug.v1.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Api(value = "debug api", description = "")
@Path("/api/debug/v1")
public abstract class DebugResource {

    @ApiOperation(value = "get-string", notes = "get string")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/get-string")
    public abstract String getString();

    @ApiOperation(value = "get-exception", notes = "get exception")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/get-exception")
    public abstract String getException();
}
