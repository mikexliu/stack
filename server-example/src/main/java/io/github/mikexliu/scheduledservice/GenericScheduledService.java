package io.github.mikexliu.scheduledservice;

import com.google.inject.Inject;
import io.github.mikexliu.stack.guice.aop.timed.Timed;
import io.github.mikexliu.stack.util.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.UsersCache;

import java.util.concurrent.TimeUnit;

public class GenericScheduledService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(GenericScheduledService.class);

    private final UsersCache usersCache;

    @Inject
    public GenericScheduledService(final UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(10, 60, TimeUnit.SECONDS);
    }

    @Timed
    @Override
    public void run() {
        log.info("Ran " + getClass());
    }
}
