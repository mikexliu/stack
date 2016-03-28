package io.github.mikexliu.stack.guice.aop.timed;

import com.google.common.base.Stopwatch;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TimedInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class callingClass = this.getClass(invocation.getThis().getClass());
        Logger log = LoggerFactory.getLogger(callingClass);
        Stopwatch stopWatch = Stopwatch.createStarted();

        Object var5;
        try {
            log.info(String.format("%s started", new Object[]{callingClass}));
            var5 = invocation.proceed();
        } finally {
            log.info(String.format("%s finished; took %s seconds", new Object[]{callingClass, Long.valueOf(stopWatch.elapsed(TimeUnit.SECONDS))}));
        }

        return var5;
    }

    private Class<?> getClass(Class<?> callingClass) {
        return callingClass.getSimpleName().contains("EnhancerByGuice")?callingClass.getSuperclass():callingClass;
    }
}
