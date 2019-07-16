package cn.ideabuffer.async.test.spring.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author sangjian.sj
 * @date 2019/07/11
 */
public class TestMethodInterceptor implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(TestMethodInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object bean = invocation.getThis();
        logger.debug("before method:{}", invocation.getMethod().getName());
        Object result = invocation.proceed();
        logger.debug("after method:{}", invocation.getMethod().getName());
        return result;
    }
}
