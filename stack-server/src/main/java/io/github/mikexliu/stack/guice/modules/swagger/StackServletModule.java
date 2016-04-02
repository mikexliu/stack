package io.github.mikexliu.stack.guice.modules.swagger;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import io.github.mikexliu.stack.guice.modules.swagger.handler.exception.ThrowableResponseHandler;
import io.github.mikexliu.stack.guice.modules.swagger.handler.exception.ThrowableResponseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StackServletModule extends ServletModule {

    private static final Logger log = LoggerFactory.getLogger(StackServletModule.class);

    private final boolean corsEnabled;
    private final ThrowableResponseHandler throwableResponseHandler;

    public StackServletModule(final boolean corsEnabled, final ThrowableResponseHandler throwableResponseHandler) {
        this.corsEnabled = corsEnabled;
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
                if (corsEnabled && response instanceof HttpServletResponse) {
                    addCorsHeader((HttpServletResponse) response);
                }

                chain.doFilter(request, response);
            }

            private void addCorsHeader(HttpServletResponse response) {
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
                response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
                response.addHeader("Access-Control-Max-Age", "1728000");
            }

            @Override
            public void destroy() {
            }
        });

        bind(ThrowableResponseMapper.class).toInstance(new ThrowableResponseMapper(throwableResponseHandler));
    }
}