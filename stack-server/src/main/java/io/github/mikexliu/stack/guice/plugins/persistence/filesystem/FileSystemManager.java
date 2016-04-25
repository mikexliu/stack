package io.github.mikexliu.stack.guice.plugins.persistence.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Uses best effort to serialize and deserialize the given class using ObjectMapper.
 */
public class FileSystemManager {

    private static final Logger log = LoggerFactory.getLogger(FileSystemManager.class);

    private final File fileSystem;

    FileSystemManager(final File fileSystem) {
        this.fileSystem = fileSystem;
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
