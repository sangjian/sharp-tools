package cn.ideabuffer.async.annotation;

import java.lang.annotation.*;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Async {

    /**
     * 指定执行器名称
     * @return
     */
    String value() default "";

    long timeout() default 0;
}
