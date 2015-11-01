package web;

import java.io.InputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

import example.container.MyItem;

public class StackTest {

    private static int port;
    private static Stack stack;

    @BeforeClass
    public static void beforeClass() throws Exception {
        final ServerSocket s = new ServerSocket(0);
        port = s.getLocalPort();
        final Properties testProperties = new Properties();
        testProperties.put("port", Integer.toString(port));

        final Injector injector = Guice.createInjector(new AbstractModule() {

            @Override
            protected void configure() {
                final Map<String, MyItem> items = new HashMap<>();
                bind(new TypeLiteral<Map<String, MyItem>>() {
                }).annotatedWith(Names.named("items")).toInstance(items);
            }
        });

        stack = new Stack(injector);
        stack.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        stack.stop();
    }

    private String create(final String data) throws Exception {
        final ProcessBuilder processBuilder = new ProcessBuilder("curl", "-X", "POST",
                "http://localhost:" + port + "/api/my-resource/", "-d", "{\"data\":\"" + data + "\"}", "--header",
                "Content-Type: application/json");
        final Process process = processBuilder.start();
        final InputStream inputStream = process.getInputStream();
        final String _id = new String(ByteStreams.toByteArray(inputStream));
        return _id;
    }

    private MyItem read(final String _id) throws Exception {
        final ProcessBuilder processBuilder = new ProcessBuilder("curl", "-X", "GET",
                "http://localhost:" + port + "/api/my-resource/" + _id);
        final Process process = processBuilder.start();
        final InputStream inputStream = process.getInputStream();
        final String data = new String(ByteStreams.toByteArray(inputStream));
        System.out.println(data);
        return null;
    }
    
    private void update() throws Exception {

    }

    private void delete() throws Exception {

    }
    
    @Test
    public void testCreateRead() throws Exception {
        final String data = UUID.randomUUID().toString() + "_" + UUID.randomUUID().toString();
        final String _id = create(data);
    }
}
