package io.github.mikexliu;

import io.github.mikexliu.stack.util.AbstractScheduledService;

/**
 * Created by mliu on 3/27/16.
 */
public class GenericScheduledService extends AbstractScheduledService {

    @Override
    protected Scheduler scheduler() {
        return null;
    }

    @Override
    public void run() {

    }
}
