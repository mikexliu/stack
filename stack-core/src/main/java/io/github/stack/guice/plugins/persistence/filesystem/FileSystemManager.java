package io.github.stack.guice.plugins.persistence.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Uses best effort to serialize and deserialize the given class using ObjectMapper.
 */
public class FileSystemManager {

    private static final Logger log = LoggerFactory.getLogger(FileSystemManager.class);

    private final ObjectMapper objectMapper;
    private final File fileSystem;

    /**
     * Package private constructor to prevent creation of this object.
     */
    FileSystemManager(final ObjectMapper objectMapper, final File fileSystem) {
        this.objectMapper = objectMapper;
        this.fileSystem = fileSystem;
    }

    public <T> String create(final T object) {
        try {
            final String serializedObject = objectMapper.writeValueAsString(object);
            final String id = Integer.toHexString(object.hashCode());

            Files.write(new File(fileSystem, id).toPath(), serializedObject.getBytes());

            log.info("Created: " + serializedObject);
            return id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T read(final String id, final Class<T> clazz) {
        try {
            final String serializedObject = new String(Files.readAllBytes(new File(fileSystem, id).toPath()));

            log.info("Read: " + serializedObject);
            return objectMapper.readValue(serializedObject, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void update(final String id, final T object) {
        try {
            final String serializedObject = objectMapper.writeValueAsString(object);

            log.info("Updated: " + serializedObject);
            Files.write(new File(fileSystem, id).toPath(), serializedObject.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(final String id) {
        try {
            Files.delete(new File(fileSystem, id).toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
