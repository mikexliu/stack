package web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.DispatcherType;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scopes;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * TODO: use swagger-core annotations explicitly (remove BeanConfig and use
 * better scanner)
 * https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#api
 * 
 * TODO: remove Server from here, add to constructor
 * 
 * TODO: make the ResponseThrowableHandler easier to inject or create
 */
public class Stack {

    private static final Logger log = LoggerFactory.getLogger(Stack.class);

    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(Stack.class.getResourceAsStream("/stack.properties"));

            Resource.setDefaultUseCaches(false);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load stack.properties", e);
        }
    }

    private final Server server;
    private final Injector injector;

    private final ResponseThrowableHandler responseThrowableHandler;

    /**
     * 
     */
    public Stack() {
        this(null, null);
    }

    /**
     * 
     * @param injector
     */
    public Stack(final Injector injector) {
        this(injector, null);
    }

    /**
     * 
     * @param properties
     */
    public Stack(final Properties properties) {
        this(null, properties);
    }

    /**
     * 
     * @param injector
     * @param properties
     */
    public Stack(final Injector injector, final Properties properties) {
        if (properties != null) {
            Stack.properties.putAll(properties);
        }

        if (injector != null) {
            this.injector = injector.createChildInjector(new StackModule());
        } else {
            this.injector = Guice.createInjector(new StackModule());
        }

        if (this.injector.getExistingBinding(Key.get(ResponseThrowableHandler.class)) == null) {
            responseThrowableHandler = new ResponseThrowableHandler() {

                @Override
                public Response handleThrowable(final Throwable throwable) {
                    log.warn("Internal Server Exception", throwable);
                    return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).build();
                }
            };
        } else {
            responseThrowableHandler = this.injector.getInstance(ResponseThrowableHandler.class);
        }

        this.server = new Server(Integer.parseInt(Stack.properties.getProperty("port")));
    }

    public void start() throws Exception {
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(Stack.properties.getProperty("version"));
        beanConfig.setResourcePackage(Joiner.on(",").join(
                getResources().stream().map(Class::getPackage).map(Package::getName).collect(Collectors.toSet())));
        beanConfig.setScan(true);
        beanConfig.setBasePath("/");
        beanConfig.setTitle(Stack.properties.getProperty("swagger.title"));
        beanConfig.setDescription(Stack.properties.getProperty("swagger.description"));

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(buildSwaggerContext());
        handlers.addHandler(buildContext());

        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    private Set<Class<?>> getResources() {
        final Set<Key<?>> keys = injector.getAllBindings().keySet();
        final Set<Class<?>> resources = Sets.newHashSet();
        for (final Key<?> key : keys) {
            final Class<?> classType = key.getTypeLiteral().getRawType();
            if (classType.isAnnotationPresent(Path.class)) {
                resources.add(classType);
            }
        }
        return resources;
    }

    private ContextHandler buildContext() {
        final Set<String> resources = Sets.newHashSet();
        resources.add(ApiListingResource.class.getPackage().getName());
        final String[] packages = resources.toArray(new String[resources.size()]);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(packages);

        final ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(servletHolder, "/*");

        final Injector childInjector = injector.createChildInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                bind(GuiceContainer.class).in(Scopes.SINGLETON);

                final Map<String, String> parameters = Maps.newHashMap();
                parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                        ResponseThrowableMapper.class.getPackage().getName());
                parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
                serve("/*").with(GuiceContainer.class, parameters);

                bind(ResponseThrowableMapper.class).toInstance(new ResponseThrowableMapper(responseThrowableHandler));
            }
        });

        final FilterHolder guiceFilter = new FilterHolder(childInjector.getInstance(GuiceFilter.class));
        servletContextHandler.addFilter(guiceFilter, String.format("/%s/*", Stack.properties.getProperty("api.prefix")),
                EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addServlet(DefaultServlet.class, "/");
        servletContextHandler.addEventListener(new GuiceServletContextListener() {
            @Override
            protected Injector getInjector() {
                return childInjector;
            }
        });

        return servletContextHandler;
    }

    private ContextHandler buildSwaggerContext() throws URISyntaxException {
        final ResourceHandler swaggerUIResourceHandler = new ResourceHandler();
        swaggerUIResourceHandler.setResourceBase(getClass().getClassLoader()
                .getResource(Stack.properties.getProperty("swagger.dist.folder")).toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath(String.format("/%s/", Stack.properties.getProperty("docs.prefix")));
        swaggerUIContext.setHandler(swaggerUIResourceHandler);

        return swaggerUIContext;
    }

    @Provider
    private static class ResponseThrowableMapper implements ExceptionMapper<Throwable> {

        private final ResponseThrowableHandler responseTHrowableHandler;

        public ResponseThrowableMapper(final ResponseThrowableHandler responseThrowableHandler) {
            this.responseTHrowableHandler = responseThrowableHandler;
        }

        @Override
        public Response toResponse(final Throwable throwable) {
            return responseTHrowableHandler.handleThrowable(throwable);
        }
    }

    /**
     * Interface to provide a custom exception handler for
     * JsonProcessingExceptions
     */
    public static interface ResponseThrowableHandler {
        public Response handleThrowable(final Throwable throwable);
    }
}

class StackModule extends AbstractModule {

    private Map<Method, Method> resourceToContainer;

    protected void configure() {
        final Reflections reflections = new Reflections();
        final Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Path.class);
        for (final Class<?> resource : classes) {
            if (Modifier.isAbstract(resource.getModifiers())) {
                Class<?> containerFound = null;
                for (final Class<?> container : classes) {
                    if (resource.isAssignableFrom(container) && resource != container) {
                        if (containerFound == null) {
                            containerFound = container;
                        } else {
                            throw new IllegalStateException(
                                    "Found multiple implementations of " + resource + " (can only accept one)");
                        }
                    }
                }
                if (containerFound == null) {
                    throw new IllegalStateException("Found no implementations of " + resource);
                } else {
                    bindResourceToContainer(resource, containerFound);
                }
            }
        }
    }

    private final void bindResourceToContainer(final Class<?> resource, final Class<?> container) {
        Preconditions.checkArgument(Modifier.isFinal(container.getModifiers()),
                container + " must be declared as final");

        this.resourceToContainer = Maps.newHashMap();

        final Set<Method> abstractMethods = Sets.newHashSet(resource.getMethods()).stream()
                .filter(method -> Modifier.isAbstract(method.getModifiers())).collect(Collectors.toSet());

        for (final Method resourceMethod : abstractMethods) {
            final Method containerMethod = findMatchingMethod(container, resourceMethod);
            this.resourceToContainer.put(resourceMethod, containerMethod);
        }

        bindResource(bindContainer(container), resource);
    }

    private final Method findMatchingMethod(final Class<?> classType, final Method matchingMethod) {
        for (final Method method : classType.getMethods()) {
            if (method.getName().equals(matchingMethod.getName())
                    && matchParameters(method.getParameters(), matchingMethod.getParameters())) {
                return method;
            }
        }
        return null;
    }

    private final boolean matchParameters(final Parameter[] parameters1, final Parameter[] parameters2) {
        if (parameters1.length != parameters2.length) {
            return false;
        } else {
            for (int i = 0; i < parameters1.length; i++) {
                final Parameter parameter1 = parameters1[i];
                final Parameter parameter2 = parameters2[i];

                if (!parameter1.getType().equals(parameter2.getType())) {
                    return false;
                }
            }
            return true;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Object bindContainer(final Class container) {
        try {
            final Object containerInstance = container.newInstance();
            requestInjection(containerInstance);
            bind(container).toInstance(container.cast(containerInstance));
            return containerInstance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private final Object bindResource(final Object containerInstance, Class resource) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(resource);
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return Modifier.isAbstract(method.getModifiers());
            }
        });

        final MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object b, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                final Method containerMethod = resourceToContainer.get(thisMethod);
                if (containerMethod != null) {
                    return containerMethod.invoke(containerInstance, args);
                } else {
                    throw new IllegalAccessException(
                            thisMethod + " is not implemented in " + containerInstance.getClass() + " via interface");
                }
            }
        };

        try {
            final Object resourceInstance = resource.cast(factory.create(new Class<?>[0], new Object[0], handler));
            bind(resource).toInstance(resource.cast(resourceInstance));
            return resourceInstance;
        } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
