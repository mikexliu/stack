package resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.spi.resource.Singleton;

@Singleton
public abstract class Resource<I extends Item> {
	
	protected final Container<I> _container;
	
	public Resource() {
		_container = null;
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract void create(final I item);
	
	@GET
	@Path("/{_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public abstract I read(@PathParam("_id") final String _id);
	
	@PUT
	@Path("/{_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public abstract I update(@PathParam("_id") final String _id, final I item);
	
	@DELETE
	@Path("/{_id}")
	public abstract void delete(@PathParam("_id") final String _id);
}
