package io.github.mikexliu.guice.modules;

import com.google.inject.Scopes;
import io.github.mikexliu.stack.guice.plugins.StackPlugin;
import service.UsersCache;

public class ServiceModule extends StackPlugin {

    @Override
    protected void configure() {
        bind(UsersCache.class).in(Scopes.SINGLETON);
    }
}
