package stack.client;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import example.resource.v1.FirstResource;
import stack.helper.StackTestServer;

import static org.junit.Assert.*;

import org.junit.After;

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
    public void testConstructor() {
        final FirstResource myResourceClient = stackClient.getClient(FirstResource.class);
        assertNotNull(myResourceClient);
        
        // TODO: delete these and pull into their own tests
        System.out.println("create");
        myResourceClient.create(null);

        System.out.println("read");
        myResourceClient.read(null);

        System.out.println("update");
        myResourceClient.update(null, null);

        System.out.println("delete");
        myResourceClient.delete(null);
    }

}
