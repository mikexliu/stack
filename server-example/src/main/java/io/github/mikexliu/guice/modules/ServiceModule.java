package io.github.mikexliu.guice.modules;

import com.google.inject.Scopes;
import io.github.mikexliu.stack.guice.plugins.app.AppPlugin;
import service.UsersCache;

public class ServiceModule extends AppPlugin {

    @Override
    protected void configure() {
        bind(UsersCache.class).in(Scopes.SINGLETON);
    }
}
