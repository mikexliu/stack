package example;

import inject.Module;
import web.SwaggerModule;

import com.google.inject.servlet.ServletModule;

public class MyModule extends Module<MyResource, MyContainer> {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);

        install(new ServletModule());
        install(new SwaggerModule());
    }
}