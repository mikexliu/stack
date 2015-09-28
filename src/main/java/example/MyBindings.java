package example;

public interface MyBindings {
    public void create(final MyItem item);

    public MyItem read(final String _id);

    public MyItem update(final String _id, final MyItem item);

    public void delete(final String _id);

    public String call();
}
