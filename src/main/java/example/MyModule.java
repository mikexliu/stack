package example;

import inject.Module;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

import web.SwaggerModule;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class MyModule extends Module<MyResource, MyContainer> {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);

        install(new ServletModule() {
            @Override
            protected void configureServlets() {
                bind(GuiceContainer.class);
                bind(ObjectMapper.class).in(Scopes.SINGLETON);
                serve("/*").with(GuiceContainer.class);
            }

            @Provides
            @Singleton
            JacksonJsonProvider jacksonJsonProvider(ObjectMapper mapper) {
                return new JacksonJsonProvider(mapper);
            }
        });
        install(new SwaggerModule());
    }
}