package io.github.mikexliu.stack.guice.modules;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mliu on 3/27/16.
 */
public class SwaggerServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(GuiceContainer.class).in(Scopes.SINGLETON);

        final Map<String, String> parameters = new HashMap<>();
        parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                ResponseThrowableMapper.class.getPackage().getName());
        parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        serve("/*").with(GuiceContainer.class, parameters);
        filter("/*").through(new Filter() {

            @Override
            public void init(final FilterConfig filterConfig) throws ServletException {

            }

            @Override
            public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
                if (response instanceof HttpServletResponse) {
                    HttpServletResponse alteredResponse = ((HttpServletResponse) response);
                    addCorsHeader(alteredResponse);
                }

                chain.doFilter(request, response);
            }

            /**
             * TODO: make this optional
             * @param response
             */
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

        bind(ResponseThrowableMapper.class).toInstance(new ResponseThrowableMapper());
    }

    private static class ResponseThrowableMapper implements ExceptionMapper<Throwable> {

        @Override
        public Response toResponse(final Throwable throwable) {
            return null;
        }
    }
}
