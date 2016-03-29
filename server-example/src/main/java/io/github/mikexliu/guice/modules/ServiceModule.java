package io.github.mikexliu.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import service.UsersCache;

/**
 * Created by mliu on 3/28/16.
 */
public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UsersCache.class).in(Scopes.SINGLETON);
    }
}
