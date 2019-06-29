package cn.ideabuffer.async.annotation;

import java.lang.annotation.*;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Async {

    String value() default "defaultAsyncExecutor";

    long timeout() default 0;
}
