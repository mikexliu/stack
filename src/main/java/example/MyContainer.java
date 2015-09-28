package example;

import resources.Container;

public final class MyContainer extends Container implements MyBindings {

    @Override
    public void create(final MyItem item) {
        System.out.println("create " + item);
    }

    @Override
    public MyItem read(final String _id) {
        System.out.println("read " + _id);
        return null;
    }

    @Override
    public MyItem update(final String _id, final MyItem item) {
        System.out.println("update " + _id + " with " + item);
        return null;
    }

    @Override
    public void delete(final String _id) {
        System.out.println("delete " + _id);
    }

    public String call() {
        return "call";
    }
}
