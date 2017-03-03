package io.github.stack.guice.modules.swagger;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.github.stack.guice.modules.swagger.handler.exception.ThrowableResponseHandler;
import io.github.stack.guice.modules.swagger.handler.exception.ThrowableResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StackServletModule extends ServletModule {
    
    private static final Logger log = LoggerFactory.getLogger(StackServletModule.class);
    
    private final ThrowableResponseHandler throwableResponseHandler;
    
    public StackServletModule(final ThrowableResponseHandler throwableResponseHandler) {
        this.throwableResponseHandler = throwableResponseHandler;
    }
    
    @Override
    protected void configureServlets() {
        bind(GuiceContainer.class).in(Scopes.SINGLETON);
        
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                ThrowableResponseMapper.class.getPackage().getName());
        parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE.toString());
        serve("/*").with(GuiceContainer.class, parameters);
        filter("/*").through(new Filter() {
            
            @Override
            public void init(final FilterConfig filterConfig) throws ServletException {
            }
            
            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
                try {
                    chain.doFilter(request, response);
                } catch (Exception e) {
                    final Response handledResponse = throwableResponseHandler.respond(e);
                    if (response instanceof HttpServletResponse) {
                        final HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                        handledResponse.getHeaders().keySet().forEach(key -> httpServletResponse.addHeader(key, handledResponse.getHeaderString(key)));
                        handledResponse.getCookies().entrySet().forEach(entry -> httpServletResponse.addCookie(new Cookie(entry.getKey(), entry.getValue().getValue())));
                        httpServletResponse.getWriter().write(handledResponse.getEntity().toString());
                        httpServletResponse.setStatus(handledResponse.getStatus());
                    }
                }
            }
            
            @Override
            public void destroy() {
            }
        });
        
        if (throwableResponseHandler != null) {
            bind(ThrowableResponseMapper.class).toInstance(new ThrowableResponseMapper(throwableResponseHandler));
        }
    }
}
