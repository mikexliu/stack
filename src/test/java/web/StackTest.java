package web;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import example.MyItem;

/**
 * Functional tests
 */
public class StackTest {

    @Rule
    public ExpectedException exceptedException = ExpectedException.none();

    private static ObjectMapper om = new ObjectMapper();

    private static int port;
    private static Stack stack;

    @BeforeClass
    public static void beforeClass() throws Exception {
        port = 5555;
        final Properties testProperties = new Properties();
        testProperties.put("port", Integer.toString(port));

        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final Map<String, MyItem> items = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {})
                    .annotatedWith(Names.named("items"))
                    .toInstance(items);
            }
        });
        
        stack = new Stack(injector);
        stack.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        stack.stop();
    }

    private Process curl(final String... args) throws IOException, InterruptedException {
        final List<String> curlArgs = new LinkedList<>();
        curlArgs.add("curl");
        curlArgs.addAll(Arrays.asList(args));
        final ProcessBuilder processBuilder = new ProcessBuilder(curlArgs);
        final Process process = processBuilder.start();
        process.waitFor();
        assertEquals(0, process.exitValue());
        return process;
    }

    private String create(final String data) throws Exception {
        final Process process = curl("-X", "POST", "http://localhost:" + port + "/api/my-resource/", "-d",
                "{\"data\":\"" + data + "\"}", "--header", "Content-Type: application/json");
        final InputStream inputStream = process.getInputStream();
        final String _id = new String(ByteStreams.toByteArray(inputStream));
        return _id;
    }

    /**
     * 
     * @param _id
     * @return
     * @throws Exception
     *             if the data read is bad or doesn't exist
     */
    private MyItem read(final String _id) throws Exception {
        final Process process = curl("-X", "GET", "http://localhost:" + port + "/api/my-resource/" + _id);
        final InputStream inputStream = process.getInputStream();
        final MyItem item = om.readValue(inputStream, MyItem.class);
        return item;
    }

    private void update(final String _id, final String data) throws Exception {
        curl("-X", "PUT", "http://localhost:" + port + "/api/my-resource/" + _id, "-d", "{\"data\": \"" + data + "\"}",
                "--header", "Content-Type: application/json");
    }

    private void delete(final String _id) throws Exception {
        curl("-X", "DELETE", "http://localhost:" + port + "/api/my-resource/" + _id);
    }

    @Test
    public void testCreateRead() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = create(data);
        final String readData = read(_id).data;
        assertEquals(data, readData);
    }

    @Test
    public void testCreateUpdate() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = create(data);
        final String updatedData = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        update(_id, updatedData);
        final String readData = read(_id).data;
        assertEquals(updatedData, readData);
    }

    @Test
    public void testCreateDelete() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = create(data);
        final String readData = read(_id).data;
        assertEquals(data, readData);
        delete(_id);

        exceptedException.expect(JsonMappingException.class);
        exceptedException.expectMessage("No content to map");

        read(_id);
    }
}
