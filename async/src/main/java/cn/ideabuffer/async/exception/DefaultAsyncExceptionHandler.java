package cn.ideabuffer.async.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author sangjian.sj
 * @date 2019/07/30
 */
public class DefaultAsyncExceptionHandler implements AsyncExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAsyncExceptionHandler.class);

    @Override
    public void handleException(Throwable ex, Method method, Object... params) {
        logger.error("Unexpected error occurred invoking async method '{}', params:{}.", method, params, ex);
    }
}
