package stack.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import example.MyItem;
import example.resource.v1.FirstResource;
import example.resource.v2.SecondResource;
import stack.helper.StackTestServer;

public class StackClientTest {

    private StackClient stackClient;

    @Rule
    public ExpectedException exceptedException = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() throws Exception {
        StackTestServer.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        StackTestServer.stop();
    }

    @Before
    public void before() {
        stackClient = new StackClient("http", "localhost", StackTestServer.getPort());
    }

    @After
    public void after() {
        stackClient = null;
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
}
