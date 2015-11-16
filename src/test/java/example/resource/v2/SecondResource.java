package example.resource.v2;

import example.MyItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(value = "v2/my-resource")
@Path("/api/v2/my-resource")
public abstract class SecondResource {

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

            @ApiParam("data description")
            @QueryParam("data")
            final String data);

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
