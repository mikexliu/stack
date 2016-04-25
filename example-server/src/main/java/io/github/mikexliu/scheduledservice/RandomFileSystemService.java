package io.github.mikexliu.scheduledservice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.github.mikexliu.collect.User;
import io.github.mikexliu.stack.guice.plugins.persistence.filesystem.FileSystemManager;
import io.github.mikexliu.stack.guice.plugins.services.scheduledservice.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomFileSystemService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(AgingService.class);

    private final Provider<FileSystemManager> fileSystemManagerProvider;

    private String id;

    @Inject
    public RandomFileSystemService(final Provider<FileSystemManager> fileSystemManagerProvider) {
        this.fileSystemManagerProvider = fileSystemManagerProvider;

        this.id = null;
    }

    @Override
    public void runOneIteration() {
        final FileSystemManager fileSystemManager = fileSystemManagerProvider.get();

        int action = new Random().nextInt(3);
        if (action > 0) {
            if (id != null) {
                // read user
                final User user = fileSystemManager.read(id, User.class);
                user.growUp();

                // update user
                fileSystemManager.update(id, user);
            } else {
                action = 0;
            }
        }

        if (action == 0) {
            if (id == null) {
                // create if no user
                final User user = new User();
                user.name = Integer.toHexString(user.hashCode());
                user.age = 0;
                id = fileSystemManager.create(user);
            } else {
                // delete if user exists
                fileSystemManager.delete(id);
                id = null;
            }
        }
    }

    @Override
    public com.google.common.util.concurrent.AbstractScheduledService.Scheduler scheduler() {
        return com.google.common.util.concurrent.AbstractScheduledService.Scheduler.newFixedRateSchedule(1, 1, TimeUnit.SECONDS);
    }
}
