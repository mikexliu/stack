package example;

public interface MyInterface {
    public String create(final MyItem item);

    public MyItem read(final String _id);

    public void update(final String _id, final MyItem item);

    public void delete(final String _id);
}
