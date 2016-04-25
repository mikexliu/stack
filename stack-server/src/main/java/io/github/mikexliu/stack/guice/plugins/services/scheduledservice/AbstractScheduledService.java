package io.github.mikexliu.stack.guice.plugins.services.scheduledservice;

/**
 * TODO: rewrite to support start/stop (creates new instances)
 */
public abstract class AbstractScheduledService extends com.google.common.util.concurrent.AbstractScheduledService implements Runnable {

    @Override
    public void runOneIteration() throws Exception {
        this.run();
    }
}
