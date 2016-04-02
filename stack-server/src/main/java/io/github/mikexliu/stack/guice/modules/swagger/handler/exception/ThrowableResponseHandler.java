package io.github.mikexliu.stack.guice.modules.swagger.handler.exception;

import javax.ws.rs.core.Response;

public interface ThrowableResponseHandler {
    Response respond(final Throwable throwable);
}