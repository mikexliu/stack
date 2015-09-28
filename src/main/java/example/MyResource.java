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

import resources.Resource;

/**
 * 
 * @author mike
 */
@Path("/my-resource")
public abstract class MyResource extends Resource implements MyBindings {
	
	@GET
	@Path("/call")
	public abstract String call();

	@Override
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
    public abstract void create(final MyItem item);
	
	@Override
	@GET
	@Path("/{_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract MyItem read(@PathParam("_id") final String _id);
	
	@Override
	@PUT
	@Path("/{_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract MyItem update(@PathParam("_id") final String _id, final MyItem item);
	
	@Override
	@DELETE
	@Path("/{_id}")
	public abstract void delete(@PathParam("_id") final String _id);
}
