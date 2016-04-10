package io.github.mikexliu.stack.guice.plugins.app.scheduledservice;

public abstract class AbstractScheduledService extends com.google.common.util.concurrent.AbstractScheduledService implements Runnable {

    @Override
    public void runOneIteration() throws Exception {
        this.run();
    }
}
