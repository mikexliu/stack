package stack.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import example.MyItem;
import example.helper.StackServer;
import example.resource.v1.FirstResource;
import example.resource.v2.SecondResource;
import example.resource.v3.ThirdResource;

public class StackClientTest {
    
    private static String endpoint = "localhost";
    private static int port = 5555;

    private static StackClient stackClient;
    private static StackServer stackServer;

    @BeforeClass
    public static void beforeClass() throws Exception {
        stackServer = new StackServer(port);
        stackServer.start();
        
        stackClient = new StackClient("http", endpoint, port);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        stackServer.stop();
    }

    @Test
    public void testFirstResource() {
        final FirstResource myResourceClient = stackClient.getClient(FirstResource.class);
        assertNotNull(myResourceClient);

        final MyItem myItem = new MyItem();
        myItem.data = "data";

        final String id = myResourceClient.create(myItem);

        final MyItem readItem = myResourceClient.read(id);

        readItem.data = "new data";
        myResourceClient.update(readItem._id, readItem);

        final MyItem updatedItem = myResourceClient.read(id);
        assertEquals(readItem.data, updatedItem.data);

        myResourceClient.delete(updatedItem._id);
        final MyItem deletedItem = myResourceClient.read(id);
        assertEquals(null, deletedItem);
    }

    @Test
    public void testSecondResource() {
        final SecondResource myResourceClient = stackClient.getClient(SecondResource.class);
        assertNotNull(myResourceClient);

        final MyItem myItem = new MyItem();
        myItem.data = "data";

        final String id = myResourceClient.create(myItem);

        final MyItem readItem = myResourceClient.read(id);

        readItem.data = "new data";
        myResourceClient.update(readItem._id, readItem.data);

        final MyItem updatedItem = myResourceClient.read(id);
        assertEquals(readItem.data, updatedItem.data);

        myResourceClient.delete(updatedItem._id);
        final MyItem deletedItem = myResourceClient.read(id);
        assertEquals(null, deletedItem);
    }

    @Test
    public void testThirdResource() {
        final ThirdResource myResourceClient = stackClient.getClient(ThirdResource.class);
        assertNotNull(myResourceClient);

        final MyItem myItem = new MyItem();
        myItem.data = "data";

        final String id = myResourceClient.create(myItem);

        final MyItem readItem = myResourceClient.read(id);

        readItem.data = "new data";
        myResourceClient.update(readItem._id, readItem.data);

        final MyItem updatedItem = myResourceClient.read(id);
        assertEquals(readItem.data, updatedItem.data);

        myResourceClient.delete(updatedItem._id);
        final MyItem deletedItem = myResourceClient.read(id);
        assertEquals(null, deletedItem);
    }
}
