package example.container.v3;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import example.Main;
import example.MyItem;
import example.resource.v2.SecondResource;
import example.resource.v3.ThirdResource;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.UUID;

public final class ThirdContainer extends ThirdResource {

    /**
     * This object is injected from the top-level injector in {@link Main}.
     */
    @Inject
    @Named("itemsv2")
    Map<String, MyItem> items;

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
     * <p>
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
    public Response update(final String _id, final String data) {
        if (!items.containsKey(_id)) {
            return Response.status(Status.NOT_FOUND).build();
        }

        final MyItem item = items.get(_id);
        item.data = data;
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