package io.github.mikexliu.stack.guice.plugins.persistence.filesystem;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.github.mikexliu.stack.guice.plugins.StackPlugin;

import java.io.File;

public class FileSystemPlugin extends StackPlugin {

    private final File fileSystem;

    public FileSystemPlugin() {
        this(Files.createTempDir());
    }

    public FileSystemPlugin(final String path) {
        this(new File(path));
    }

    public FileSystemPlugin(final File file) {
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
        return new FileSystemManager(fileSystem);
    }
}
