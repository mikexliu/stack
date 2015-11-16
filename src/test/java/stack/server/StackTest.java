package stack.server;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonMappingException;

import stack.helper.StackTestServer;

/**
 * Functional tests
 */
public class StackTest {

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

    @Test
    public void testCreateReadFirstResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createFirstResource(data);
        final String readData = StackTestServer.readFirstResource(_id).data;
        assertEquals(data, readData);
    }

    @Test
    public void testCreateUpdateFirstResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createFirstResource(data);
        final String updatedData = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        StackTestServer.updateFirstResource(_id, updatedData);
        final String readData = StackTestServer.readFirstResource(_id).data;
        assertEquals(updatedData, readData);
    }

    @Test
    public void testCreateDeleteFirstResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createFirstResource(data);
        final String readData = StackTestServer.readFirstResource(_id).data;
        assertEquals(data, readData);
        StackTestServer.deleteFirstResource(_id);

        exceptedException.expect(JsonMappingException.class);
        exceptedException.expectMessage("No content to map");

        StackTestServer.readFirstResource(_id);
    }

    @Test
    public void testCreateReadSecondResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createSecondResource(data);
        final String readData = StackTestServer.readSecondResource(_id).data;
        assertEquals(data, readData);
    }

    @Test
    public void testCreateUpdateSecondResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createSecondResource(data);
        final String updatedData = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        StackTestServer.updateSecondResource(_id, updatedData);
        final String readData = StackTestServer.readSecondResource(_id).data;
        assertEquals(updatedData, readData);
    }

    @Test
    public void testCreateDeleteSecondResource() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = StackTestServer.createSecondResource(data);
        final String readData = StackTestServer.readSecondResource(_id).data;
        assertEquals(data, readData);
        StackTestServer.deleteSecondResource(_id);

        exceptedException.expect(JsonMappingException.class);
        exceptedException.expectMessage("No content to map");

        StackTestServer.readSecondResource(_id);
    }
    
    // TODO: make tests that "conflict" resources
}
