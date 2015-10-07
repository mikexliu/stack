package example;

import inject.StackModule;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import web.Stack.ResponseThrowableHandler;

public class MyModule extends StackModule {

    @Override
    protected void configure() {
        bindResourceToContainer(MyResource.class, MyContainer.class);

        bind(ResponseThrowableHandler.class).toInstance(new ResponseThrowableHandler() {

            @Override
            public Response handleThrowable(final Throwable throwable) {
                return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).build();
            }
        });
    }
}