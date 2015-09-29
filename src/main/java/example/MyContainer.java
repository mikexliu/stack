package example;

import java.util.Map;
import java.util.UUID;

import resources.Container;

import com.google.common.collect.Maps;

public final class MyContainer extends Container implements MyInterface {

    private Map<String, MyItem> items = Maps.newHashMap();

    @Override
    public String create(final MyItem item) {
        System.out.println("create");
        item._id = UUID.randomUUID().toString();
        items.put(item._id, item);
        return item._id;
    }

    @Override
    public MyItem read(final String _id) {
        return items.get(_id);
    }

    @Override
    public void update(final String _id, final MyItem item) {
        item._id = _id;
        items.put(item._id, item);
    }

    @Override
    public void delete(final String _id) {
        items.remove(_id);
    }
}
