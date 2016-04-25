package io.github.mikexliu.stack.guice.plugins.metrics.timed;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TimedInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TimedInterceptor.class);

    @Inject
    private MetricRegistry metricRegistry;

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Class<?> callingClass = getClass(invocation.getThis().getClass());
        final String methodName = invocation.getMethod().getName();
        final Timer timerMetric = metricRegistry.timer(MetricRegistry.name(callingClass, methodName, "timer"));
        final Timer.Context timerContext = timerMetric.time();
        try {
            log.info(String.format("%s.%s started", callingClass, methodName));
            return invocation.proceed();
        } finally {
            final long elapsed = TimeUnit.NANOSECONDS.toMillis(timerContext.stop());
            log.info(String.format("%s.%s finished; took %sms", callingClass, methodName, elapsed));
        }
    }

    private Class<?> getClass(final Class<?> callingClass) {
        if (callingClass.getSimpleName().contains("EnhancerByGuice")) {
            return callingClass.getSuperclass();
        }
        return callingClass;
    }
}
