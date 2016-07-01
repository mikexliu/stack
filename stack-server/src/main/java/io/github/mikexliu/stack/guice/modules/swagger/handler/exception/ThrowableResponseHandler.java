package io.github.mikexliu.stack.guice.modules.swagger.handler.exception;

import javax.ws.rs.core.Response;

public interface ThrowableResponseHandler {

    /**
     * Given a throwable, maps to a Response object.
     * This gives the freedom to create status codes for any exception.
     * @param throwable
     * @return
     */
    Response respond(final Throwable throwable);
}