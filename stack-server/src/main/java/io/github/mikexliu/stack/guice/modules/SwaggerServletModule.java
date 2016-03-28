package io.github.mikexliu.stack.guice.modules;

import com.google.common.collect.Maps;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Map;

/**
 * Created by mliu on 3/27/16.
 */
public class SwaggerServletModule extends ServletModule {

    @Override
    protected void configureServlets() {
        bind(GuiceContainer.class).in(Scopes.SINGLETON);

        final Map<String, String> parameters = Maps.newHashMap();
        parameters.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                ResponseThrowableMapper.class.getPackage().getName());
        parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        serve("/*").with(GuiceContainer.class, parameters);

        bind(ResponseThrowableMapper.class).toInstance(new ResponseThrowableMapper());
    }

    private static class ResponseThrowableMapper implements ExceptionMapper<Throwable> {

        @Override
        public Response toResponse(final Throwable throwable) {
            return null;
        }
    }
}
