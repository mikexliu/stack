package example;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "my-resource")
@Path("/api/my-resource")
public abstract class MyResource implements MyInterface {

    @ApiOperation(value = "create", notes = "Creates and returns the id of a JSON representation of MyItem.")
    @Override
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public abstract String create(final MyItem item);

    @ApiOperation(value = "read", notes = "Returns a JSON representation of the specified MyItem.")
    @Override
    @GET
    @Path("/{_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public abstract MyItem read(@PathParam("_id") final String _id);

    @ApiOperation(value = "update", notes = "Updates the specified MyItem with a new JSON representation.")
    @Override
    @PUT
    @Path("/{_id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public abstract Response update(@PathParam("_id") final String _id, final MyItem item);

    @ApiOperation(value = "delete", notes = "Deletes the specified MyItem.")
    @Override
    @DELETE
    @Path("/{_id}")
    public abstract Response delete(@PathParam("_id") final String _id);
}
