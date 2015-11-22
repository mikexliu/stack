package stack.server;

import example.data.User;
import example.helper.StackClientHelper;
import example.helper.StackServerHelper;
import example.resource.petstore.UserResource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This class requires access to the internet to pass since it is making a remote call.
 * TODO: use local resources to test this
 */
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
    public void testUserResource() {
        final UserResource myResourceClient = stackClientHelper.getClient("remote").getClient(UserResource.class);
        Assert.assertNotNull(myResourceClient);

        User user = new User();
        user.id = 0;
        user.username = "username";
        user.firstName = "firstName";
        user.lastName = "lastName";

        myResourceClient.createUser(user);

        User readUser = myResourceClient.getUserByName("username").readEntity(User.class);
        assertEquals(user.firstName, readUser.firstName);
        assertEquals(user.lastName, readUser.lastName);

        user.lastName = "changedLastName";
        myResourceClient.updateUser("username", user);

        readUser = myResourceClient.getUserByName("username").readEntity(User.class);
        assertEquals(user.firstName, readUser.firstName);
        assertEquals("changedLastName", readUser.lastName);
    }
}
