package example;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import resources.Container;

import com.google.common.collect.Maps;

public final class MyContainer extends Container implements MyInterface {

    private Map<String, MyItem> items = Maps.newHashMap();

    @Override
    public String create(final MyItem item) {
        item._id = UUID.randomUUID().toString();
        items.put(item._id, item);
        return item._id;
    }

    @Override
    public MyItem read(final String _id) {
        return items.get(_id);
    }

    @Override
    public Response update(final String _id, final MyItem item) {
        if (!items.containsKey(_id)) {
            return Response.status(Status.NO_CONTENT).build();
        }

        item._id = _id;
        items.put(_id, item);
        return Response.ok().build();
    }

    @Override
    public Response delete(final String _id) {
        items.remove(_id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
