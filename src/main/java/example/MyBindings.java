package example;

import resources.Item;

public interface MyBindings<I extends Item> {
    public void create(final I item);
    public I read(final String _id);
    public I update(final String _id, final I item);
    public void delete(final String _id);
}
