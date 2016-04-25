package io.github.mikexliu.stack.guice.plugins.persistence.redis;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.github.mikexliu.stack.guice.plugins.StackPlugin;

public class RedisPlugin extends StackPlugin {

    @Override
    protected void configure() {

    }

    @Singleton
    @Provides
    public RedisManager fileSystemManagerProvider() {
        return new RedisManager();
    }
}
