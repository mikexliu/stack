package io.github.mikexliu.stack.guice.plugins.app.timed;

import com.google.common.base.Stopwatch;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TimedInterceptor implements MethodInterceptor {

    final Logger log = LoggerFactory.getLogger(TimedInterceptor.class);

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Class<?> callingClass = getClass(invocation.getThis().getClass());
        final String methodName = invocation.getMethod().getName();

        final Stopwatch stopWatch = Stopwatch.createStarted();
        try {
            log.info(String.format("%s.%s started", callingClass, methodName));
            return invocation.proceed();
        } finally {
            log.info(String.format("%s.%s finished; took %sms", callingClass, methodName, stopWatch.elapsed(TimeUnit.MILLISECONDS)));
        }
    }

    private Class<?> getClass(final Class<?> callingClass) {
        if (callingClass.getSimpleName().contains("EnhancerByGuice")) {
            return callingClass.getSuperclass();
        }
        return callingClass;
    }
}
