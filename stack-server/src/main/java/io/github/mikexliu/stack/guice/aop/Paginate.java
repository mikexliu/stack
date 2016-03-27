package io.github.mikexliu.stack.guice.aop;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Paginate {
    int maxNumberResults() default 25;
}
