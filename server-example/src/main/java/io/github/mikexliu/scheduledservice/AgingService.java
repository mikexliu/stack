package io.github.mikexliu.scheduledservice;

import com.google.inject.Inject;
import io.github.mikexliu.collect.User;
import io.github.mikexliu.stack.guice.plugins.app.timed.Timed;
import io.github.mikexliu.stack.guice.plugins.stack.scheduledservice.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UsersCache;

import java.util.concurrent.TimeUnit;

public class AgingService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(AgingService.class);

    private final UsersCache usersCache;

    @Inject
    public AgingService(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(5, 15, TimeUnit.SECONDS);
    }

    @Timed
    @Override
    public void run() {
        try {
            usersCache.getAllUsers().forEach(User::growUp);
        } catch (Exception e) {
            log.warn(getClass() + " failed", e);
        }
    }
}
