package example.container;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.common.collect.Maps;

import example.resource.MyResource;

public final class MyContainer extends MyResource {

    private Map<String, MyItem> items = Maps.newHashMap();

    /**
     * Create an MyItem object
     */
    @Override
    public String create(final MyItem item) {
        item._id = UUID.randomUUID().toString();
        items.put(item._id, item);
        return item._id;
    }

    /**
     * If the item to update does not exist, returns 204
     * Otherwise, returns the object
     * 
     * This shows we can return non-Response, non-String objects.
     */
    @Override
    public MyItem read(final String _id) {
        return items.get(_id);
    }

    /**
     * If the item to update does not exist, return 404
     */
    @Override
    public Response update(final String _id, final MyItem item) {
        if (!items.containsKey(_id)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        item._id = _id;
        items.put(_id, item);
        return Response.ok(item, MediaType.APPLICATION_JSON).build();
    }

    /**
     * This is successful even if no item exists
     */
    @Override
    public Response delete(final String _id) {
        items.remove(_id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
