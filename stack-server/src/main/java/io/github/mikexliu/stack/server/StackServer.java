package io.github.mikexliu.stack.server;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.GuiceServletContextListener;
import io.github.mikexliu.stack.guice.plugins.front.timed.TimedModule;
import io.github.mikexliu.stack.guice.modules.SwaggerServletModule;
import io.github.mikexliu.stack.guice.modules.apis.ContainersModule;
import io.github.mikexliu.stack.guice.modules.apis.ResourcesModule;
import io.github.mikexliu.stack.guice.plugins.back.scheduledservice.ServicesManager;
import io.github.mikexliu.stack.guice.plugins.back.scheduledservice.ServicesManagerModule;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.ws.rs.Path;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO: use swagger-core annotations explicitly (remove BeanConfig and use
 * better scanner)
 * https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#api
 * <p>
 * TODO: remove Server from here, add to injector (every object, including
 * Properties, should come from injector; if none, then default)
 * <p>
 * TODO: make the ResponseThrowableHandler easier to inject or create
 * <p>
 * TODO: publish this to central repository.
 * https://maven.apache.org/guides/mini/guide-central-repository-upload.html
 */
public class StackServer {

    private static final Logger log = LoggerFactory.getLogger(StackServer.class);

    private static final String SWAGGER_CONTEXT_PATH = "/docs/";
    private static final String SWAGGER_FILTER = "api";

    public static final class Builder {
        private final List<String> apiPackageNames;
        private final List<Module> modules;

        private String title = "stack";
        private String version = "0.0.1";
        private String description = "stack makes it easy to generate rest endpoints.";

        private String swaggerUIDirectory = "swagger-ui";

        private int port = 5555;

        public Builder() {
            this.apiPackageNames = new LinkedList<>();
            this.modules = new LinkedList<>();
        }

        public Builder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public Builder withVersion(final String version) {
            this.version = version;
            return this;
        }

        public Builder withDescription(final String description) {
            this.description = description;
            return this;
        }

        public Builder withSwaggerUiDirectory(final String swaggerUIDirectory) {
            this.swaggerUIDirectory = swaggerUIDirectory;
            return this;
        }

        public Builder withPort(final int port) {
            this.port = port;
            return this;
        }

        public Builder withPackageNames(final Collection<String> packageNames) {
            this.apiPackageNames.addAll(packageNames);
            return this;
        }

        public Builder withApiPackageName(final String packageName) {
            this.apiPackageNames.add(packageName);
            return this;
        }

        public Builder withModules(final Collection<Module> modules) {
            this.modules.addAll(modules);
            return this;
        }

        public Builder withModules(final Module... modules) {
            this.modules.addAll(Arrays.asList(modules));
            return this;
        }

        public Builder withModule(final Module module) {
            this.modules.add(module);
            return this;
        }

        public void start() throws Exception {
            Preconditions.checkArgument(!apiPackageNames.isEmpty(), "No api package name specified; cannot find api classes.");
            Preconditions.checkArgument(!modules.isEmpty(), "No modules specified; cannot instantiate server.");
            new StackServer(this).start();
        }
    }

    private final Builder builder;

    private final Server server;
    private final Injector injector;

    private StackServer(final Builder builder) {
        Resource.setDefaultUseCaches(false); // https://www.javacodegeeks.com/2013/10/swagger-make-developers-love-working-with-your-rest-api.html

        this.builder = builder;

        // stack modules
        final List<Module> modules = builder.modules;
        modules.add(new TimedModule());
        modules.add(new ContainersModule(builder.apiPackageNames));
        final Injector stackInjector = Guice.createInjector(builder.modules);

        // meta modules
        final Collection<Module> metaModules = new LinkedList<>();
        metaModules.add(new ServicesManagerModule(stackInjector));
        metaModules.add(new ResourcesModule(builder.apiPackageNames, stackInjector));
        this.injector = stackInjector.createChildInjector(metaModules);

        this.server = new Server(builder.port);
    }

    public void start() throws Exception {
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(this.builder.version);
        beanConfig.setTitle(this.builder.title);
        beanConfig.setDescription(this.builder.description);
        beanConfig.setResourcePackage(Joiner.on(",").join(
                getResources().stream().map(Class::getPackage).map(Package::getName).collect(Collectors.toSet())));
        beanConfig.setScan(true);
        beanConfig.setBasePath("/");

        final HandlerList handlers = new HandlerList();
        handlers.addHandler(buildSwaggerContext());
        handlers.addHandler(buildContext());
        server.setHandler(handlers);

        Optional.of(this.injector.getInstance(ServicesManager.class)).ifPresent(s -> s.startAll());
        server.start();
    }

    public void stop() throws Exception {
        Optional.of(this.injector.getInstance(ServicesManager.class)).ifPresent(s -> s.stopAll());
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
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages(packages);

        final ServletHolder servletHolder = new ServletHolder(new ServletContainer(resourceConfig));
        final ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");
        servletContextHandler.addServlet(servletHolder, "/*");

        final Injector childInjector = injector.createChildInjector(new SwaggerServletModule());

        final FilterHolder guiceFilter = new FilterHolder(childInjector.getInstance(GuiceFilter.class));
        servletContextHandler.addFilter(guiceFilter, String.format("/%s/*", SWAGGER_FILTER), EnumSet.allOf(DispatcherType.class));
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
        swaggerUIResourceHandler.setResourceBase(getClass().getClassLoader().getResource(builder.swaggerUIDirectory).toURI().toString());
        final ContextHandler swaggerUIContext = new ContextHandler();
        swaggerUIContext.setContextPath(SWAGGER_CONTEXT_PATH);
        swaggerUIContext.setHandler(swaggerUIResourceHandler);

        return swaggerUIContext;
    }
}