package io.github.stack.guice.modules.swagger.handler.exception;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ThrowableResponseMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(ThrowableResponseMapper.class);

    private final ThrowableResponseHandler throwableResponseHandler;

    public ThrowableResponseMapper(final ThrowableResponseHandler throwableResponseHandler) {
        this.throwableResponseHandler = throwableResponseHandler;
    }

    @Override
    public Response toResponse(final Throwable throwable) {
        log.warn("Internal Server Error", throwable);
        return throwableResponseHandler.respond(throwable);
    }
}