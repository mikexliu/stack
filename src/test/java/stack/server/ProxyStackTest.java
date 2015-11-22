package stack.server;

import example.data.MyItem;
import example.helper.StackClientHelper;
import example.helper.StackServerHelper;
import example.resource.v1.FirstResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProxyStackTest {

    private static final String protocol = "http";
    private static final String endpoint = "localhost";
    private static final int port = 5556;

    private static StackClientHelper stackClientHelper;
    private static StackServerHelper stackServerHelper;

    @BeforeClass
    public static void beforeClass() throws Exception {
        stackServerHelper = new StackServerHelper(port, "example.container,example.resource");
        stackServerHelper.start();

        final Map<String, URL> nameToEndpoint = new HashMap<>();
        nameToEndpoint.put("remote", new URL(String.format("%s://%s:%d", protocol, endpoint, port)));

        stackClientHelper = new StackClientHelper(nameToEndpoint);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        stackServerHelper.stop();
    }

    @Test
    public void testFirstResource() {
        final FirstResource myResourceClient = stackClientHelper.getClient("remote").getClient(FirstResource.class);
        Assert.assertNotNull(myResourceClient);

        final MyItem myItem = new MyItem();
        myItem.data = "data";

        final String id = myResourceClient.create(myItem);

        final MyItem readItem = myResourceClient.read(id);

        readItem.data = "new data";
        myResourceClient.update(readItem._id, readItem);

        final MyItem updatedItem = myResourceClient.read(id);
        Assert.assertEquals(readItem.data, updatedItem.data);

        myResourceClient.delete(updatedItem._id);
        final MyItem deletedItem = myResourceClient.read(id);
        Assert.assertEquals(null, deletedItem);
    }
}
