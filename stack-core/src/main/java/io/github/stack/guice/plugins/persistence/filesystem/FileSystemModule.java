package io.github.stack.guice.plugins.persistence.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.io.File;

public class FileSystemModule extends AbstractModule {
    
    private final File fileSystem;
    
    public FileSystemModule() {
        this(Files.createTempDir());
    }
    
    public FileSystemModule(final String path) {
        this(new File(path));
    }
    
    public FileSystemModule(final File file) {
        this.fileSystem = file;
        if (!file.exists()) {
            Preconditions.checkState(file.mkdirs());
        }
        
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.isDirectory(), file + " is not a directory!");
    }
    
    @Override
    protected void configure() {
        
    }
    
    @Singleton
    @Provides
    public FileSystemManager fileSystemManagerProvider() {
        final ObjectMapper objectMapper = new ObjectMapper();
        return new FileSystemManager(objectMapper, fileSystem);
    }
}
