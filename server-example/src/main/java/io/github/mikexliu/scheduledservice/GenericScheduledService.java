package io.github.mikexliu.scheduledservice;

import io.github.mikexliu.stack.guice.aop.timed.Timed;
import io.github.mikexliu.stack.util.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class GenericScheduledService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(GenericScheduledService.class);

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
