package cn.ideabuffer.retry.annotation;

import java.lang.annotation.*;

import static cn.ideabuffer.retry.annotation.Retry.StopStrategy.NEVER_STOP;

/**
 * @author sangjian.sj
 * @date 2019/05/23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    int maxAttempts() default 3;

    int timeout() default 0;

    Class<? extends Exception>[] retryIfExceptionOfType() default {};

    boolean retryIfRuntimeException() default true;

    RetryResult[] retryIfResult() default {};

    String retryIfExpr() default "";

    StopStrategy stopStrategy() default NEVER_STOP;

    BackOff backOff() default @BackOff;

    enum StopStrategy {
        /**
         * 不停止
         */
        NEVER_STOP,
        /**
         * 指定次数停止
         */
        STOP_AFTER_ATTEMT,
        /**
         * 超过最长时间停止
         */
        STOP_AFTER_TIMEOUT;
    }

    enum RetryResult {
        IS_TRUE,
        IS_FALSE,
        IS_NULL,
        IS_NOT_NULL,
        IS_EMPTY_STRING,
        IS_NOT_EMPTY_STRING,
        IS_ZEOR,
        IS_NEGATIVE,
        IS_POSITIVE,
        IS_EMPTY_COLLECTION,
        IS_NOT_EMPTY_COLLECTION,
        IS_EMPTY_MAP,
        IS_NOT_EMPTY_MAP,
        IS_RESULT_FAILED
    }

}
