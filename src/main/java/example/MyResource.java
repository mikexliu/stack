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
    public abstract Response update(@PathParam("_id") final String _id, final MyItem item);

    @Override
    @DELETE
    @Path("/{_id}")
    public abstract Response delete(@PathParam("_id") final String _id);
}
