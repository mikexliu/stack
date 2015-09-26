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
 * TODO: See if we can turn this into an abstract class instead
 */
@Path("/my-resource")
public class MyResource extends Resource<MyItem> {
	
	@GET
	@Path("/call")
	public String call() {
		return null;
	}

	@Override
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public void create(final MyItem item) {
	    
	}
	
	@Override
	@GET
	@Path("/{_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public MyItem read(@PathParam("_id") final String _id) {
		return null;
	}
	
	@Override
	@PUT
	@Path("/{_id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public MyItem update(@PathParam("_id") final String _id, final MyItem item) {
		return null;
	}
	
	@Override
	@DELETE
	@Path("/{_id}")
	public void delete(@PathParam("_id") final String _id) {
	    
	}
}
