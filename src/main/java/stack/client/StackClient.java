package stack.client;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class StackClient implements Closeable {

    private static final ObjectMapper om = new ObjectMapper();

    private final String protocol;
    private final String endpoint;
    private final int port;

    /**
     * 
     * @param protocol
     * @param endpoint
     * @param port
     */
    public StackClient(final String protocol, final String endpoint, final int port) {
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.port = port;
    }

    public <T> T getClient(final Class<T> resourceClass) {
        final Path pathAnnotation = resourceClass.getDeclaredAnnotation(Path.class);
        Preconditions.checkNotNull(pathAnnotation,
                "Class is not annotated with @Path. Only Classes with @Path annotation may generated a client.");

        // TODO: should actually check for "/api" or whatever we've specified
        final String resource = pathAnnotation.value();
        Preconditions.checkState(resource.startsWith("/"), "Client's @Path annotation is not set correctly. Must begin with /");
        
        final String uri = String.format("%s://%s:%s/%s", protocol, endpoint, port, resource);
        
        System.out.println(uri);

        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resourceClass);
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return Modifier.isAbstract(method.getModifiers());
            }
        });

        final MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object b, Method thisMethod, Method proceed, Object[] args) throws Throwable {

                if (Void.class.equals(thisMethod.getReturnType())) {
                    
                } else {
                    final Produces producesAnnotation = thisMethod.getAnnotation(Produces.class);
                    if (producesAnnotation == null) {
                        // use annotation to figure out parsing strategy
                        // produces: json, string
                    } else {
                        // use return type to figure out parsing strategy
                    }
                }
                
                System.out.println(Arrays.asList(thisMethod.getDeclaredAnnotations()));

                return null;
            }
        };

        try {
            final T resourceInstance = resourceClass.cast(factory.create(new Class<?>[0], new Object[0], handler));
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        // TODO: to be implemented
    }
}
