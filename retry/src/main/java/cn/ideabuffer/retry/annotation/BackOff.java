package cn.ideabuffer.retry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static cn.ideabuffer.retry.annotation.BackOff.WaitStrategy.NO_WAIT;

/**
 * @author sangjian.sj
 * @date 2019/05/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BackOff {

    int fixSleepTime() default 0;

    int multiplier() default 1000;

    int increment() default 0;

    int increInitTime() default 0;

    int randomMinTime() default 0;

    int randomMaxTime() default 1000;

    int fibMaxTime() default 0;

    int expMaxTime() default 0;


    WaitStrategy waitStrategy() default NO_WAIT;

    enum WaitStrategy {
        NO_WAIT,
        FIX_WAIT,
        RANDOM_WAIT,
        INCREMENT_WAIT,
        FIBONACCI_WAIT,
        EXPONENTIAL_WAIT
    }

}
