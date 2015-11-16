package stack.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import example.MyItem;
import stack.server.Stack;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class StackTestServer {

    private static final ObjectMapper om = new ObjectMapper();

    private static int port = 5555;
    private static Stack stack = null;

    public static void start() throws Exception {
        if (stack == null) {
            final Injector injector = Guice.createInjector(new AbstractModule() {

                @Override
                protected void configure() {
                    final Map<String, MyItem> itemsv1 = new HashMap<>();
                    bind(new TypeLiteral<Map<String, MyItem>>() {
                    }).annotatedWith(Names.named("itemsv1")).toInstance(itemsv1);

                    final Map<String, MyItem> itemsv2 = new HashMap<>();
                    bind(new TypeLiteral<Map<String, MyItem>>() {
                    }).annotatedWith(Names.named("itemsv2")).toInstance(itemsv2);

                    final Map<String, MyItem> itemsv3 = new HashMap<>();
                    bind(new TypeLiteral<Map<String, MyItem>>() {
                    }).annotatedWith(Names.named("itemsv3")).toInstance(itemsv3);
                }
            });

            stack = new Stack(injector);
            stack.start();
        } else {
            throw new IllegalStateException(StackTestServer.class + " is already started. Check port " + port + ".");
        }
    }

    public static void stop() throws Exception {
        if (stack != null) {
            stack.stop();
        } else {
            throw new IllegalStateException(StackTestServer.class + " is already stopped.");
        }
    }

    public static String getEndpoint() {
        return "localhost";
    }

    public static int getPort() {
        return port;
    }

    // TODO: remove once we finish the client generating code (fully)
    private static Process curl(final String... args) throws IOException, InterruptedException {
        final List<String> curlArgs = new LinkedList<>();
        curlArgs.add("curl");
        curlArgs.addAll(Arrays.asList(args));
        final ProcessBuilder processBuilder = new ProcessBuilder(curlArgs);
        final Process process = processBuilder.start();
        process.waitFor();
        assertEquals(0, process.exitValue());
        return process;
    }

    public static String createFirstResource(final String data) throws Exception {
        final Process process = curl("-X", "POST",
                "http://localhost:" + StackTestServer.getPort() + "/api/v1/my-resource/", "-d",
                "{\"data\":\"" + data + "\"}", "--header", "Content-Type: application/json");
        final InputStream inputStream = process.getInputStream();
        final String _id = new String(ByteStreams.toByteArray(inputStream));
        return _id;
    }

    public static MyItem readFirstResource(final String _id) throws Exception {
        final Process process = curl("-X", "GET",
                "http://localhost:" + StackTestServer.getPort() + "/api/v1/my-resource/" + _id);
        final InputStream inputStream = process.getInputStream();
        final MyItem item = om.readValue(inputStream, MyItem.class);
        return item;
    }

    public static void updateFirstResource(final String _id, final String data) throws Exception {
        curl("-X", "PUT", "http://localhost:" + StackTestServer.getPort() + "/api/v1/my-resource/" + _id, "-d",
                "{\"data\": \"" + data + "\"}", "--header", "Content-Type: application/json");
    }

    public static void deleteFirstResource(final String _id) throws Exception {
        curl("-X", "DELETE", "http://localhost:" + StackTestServer.getPort() + "/api/v1/my-resource/" + _id);
    }

    public static String createSecondResource(final String data) throws Exception {
        final Process process = curl("-X", "POST",
                "http://localhost:" + StackTestServer.getPort() + "/api/v2/my-resource/", "-d",
                "{\"data\":\"" + data + "\"}", "--header", "Content-Type: application/json");
        final InputStream inputStream = process.getInputStream();
        final String _id = new String(ByteStreams.toByteArray(inputStream));
        return _id;
    }

    public static MyItem readSecondResource(final String _id) throws Exception {
        final Process process = curl("-X", "GET",
                "http://localhost:" + StackTestServer.getPort() + "/api/v2/my-resource/" + _id);
        final InputStream inputStream = process.getInputStream();
        final MyItem item = om.readValue(inputStream, MyItem.class);
        return item;
    }

    public static void updateSecondResource(final String _id, final String data) throws Exception {
        curl("-X", "PUT",
                "http://localhost:" + StackTestServer.getPort() + "/api/v2/my-resource/" + _id + "?data=" + data);
    }

    public static void deleteSecondResource(final String _id) throws Exception {
        curl("-X", "DELETE", "http://localhost:" + StackTestServer.getPort() + "/api/v2/my-resource/" + _id);
    }
}
