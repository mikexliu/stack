package example;

import resources.Container;

public class MyContainer extends Container<MyItem> implements MyBindings<MyItem> {

    @Override
    public void create(MyItem item) {

    }

    @Override
    public MyItem read(String _id) {
        return null;
    }

    @Override
    public MyItem update(final String _id, MyItem item) {
        return null;
    }

    @Override
    public void delete(String _id) {

    }

    public void call() {

    }
}
