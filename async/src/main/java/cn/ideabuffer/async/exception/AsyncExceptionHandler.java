package cn.ideabuffer.async.exception;

import java.lang.reflect.Method;

/**
 * @author sangjian.sj
 * @date 2019/07/30
 */
public interface AsyncExceptionHandler {

    void handleException(Throwable ex, Method method, Object... params);

}
