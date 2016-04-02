package io.github.mikexliu.stack.guice.modules.swagger.handler.exception;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableResponseMapper implements ExceptionMapper<Throwable> {

    private final ThrowableResponseHandler throwableResponseHandler;

    public ThrowableResponseMapper(final ThrowableResponseHandler throwableResponseHandler) {
        this.throwableResponseHandler = throwableResponseHandler;
    }

    @Override
    public Response toResponse(final Throwable throwable) {
        return throwableResponseHandler.respond(throwable);
    }
}