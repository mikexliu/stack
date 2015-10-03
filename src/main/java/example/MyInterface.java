package example;

import javax.ws.rs.core.Response;

public interface MyInterface {

    public String create(final MyItem item);

    public MyItem read(final String _id);

    public Response update(final String _id, final MyItem item);

    public Response delete(final String _id);
}
