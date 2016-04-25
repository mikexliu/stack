package io.github.mikexliu.stack.guice.plugins.persistence.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: use an interface for CRUD operations. Then, enable @Named for provider.
 */
public class RedisManager {

    private static final Logger log = LoggerFactory.getLogger(RedisManager.class);

    RedisManager() {
    }

    public <T> String create(final T object, final Class<T> clazz) {
        return null;
    }

    public <T> T read(final String id) {
        return null;
    }

    public <T> void update(final String id, final T object, final Class<T> clazz) {

    }

    public void delete(final String id) {

    }
}
