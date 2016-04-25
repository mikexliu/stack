package io.github.mikexliu.stack.guice.plugins.services.scheduledservice;

public abstract class AbstractScheduledService {

    private com.google.common.util.concurrent.AbstractScheduledService service = service();

    public final void start() {
        service = service();
        service.startAsync();
    }

    public boolean isRunning() {
        return service.isRunning();
    }

    public final com.google.common.util.concurrent.AbstractScheduledService.State state() {
        return service.state();
    }

    public final Throwable failureCause() {
        return service.failureCause();
    }

    public final void stop() {
        service.stopAsync();
    }

    private com.google.common.util.concurrent.AbstractScheduledService service() {
        final AbstractScheduledService me = this;
        return new com.google.common.util.concurrent.AbstractScheduledService() {

            @Override
            protected void runOneIteration() throws Exception {
                if (service != null && service.state() == State.RUNNING) {
                    me.runOneIteration();
                }
            }

            @Override
            protected Scheduler scheduler() {
                return me.scheduler();
            }
        };
    }

    public abstract void runOneIteration();

    public abstract com.google.common.util.concurrent.AbstractScheduledService.Scheduler scheduler();
}
