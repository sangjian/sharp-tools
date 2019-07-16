package cn.ideabuffer.async.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/15
 */
public class ParallelBeanInitMethodInterceptor implements MethodInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ParallelBeanInitMethodInterceptor.class);

    private String beanName;

    private String initMethodName;

    private Future<?> initMethodFuture;

    private Future<?> afterPropertiesSetFuture;

    private ExecutorService beanInitExecutor;

    private volatile boolean inited;

    public ParallelBeanInitMethodInterceptor(String beanName, String initMethodName, ExecutorService beanInitExecutor) {
        this.beanName = beanName;
        this.initMethodName = initMethodName;
        this.beanInitExecutor = beanInitExecutor;
        inited = false;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if(inited) {
            return invocation.proceed();
        }
        Object bean = invocation.getThis();
        boolean isInitializingBean = bean instanceof InitializingBean;
        String methodName = invocation.getMethod().getName();
        if(!methodName.equals(initMethodName) && !(isInitializingBean && "afterPropertiesSet".equals(methodName))) {
            if(afterPropertiesSetFuture != null) {
                afterPropertiesSetFuture.get();
            }
            if(initMethodFuture != null) {
                initMethodFuture.get();
            }
            inited = true;
            return invocation.proceed();
        }

        Future<?> future = beanInitExecutor.submit(() -> {
            try {
                logger.info("parallel init method start, beanName:{}, method:{}", beanName, methodName);
                invocation.proceed();
                logger.info("parallel init method end, beanName:{}, method:{}", beanName, methodName);
            } catch (Throwable throwable) {
                logger.error("init method:[{}] proceed error.", methodName, throwable);
                throw new RuntimeException(String.format("init method:[%s] proceed error.", methodName), throwable);
            }
        });

        if(isInitializingBean && "afterPropertiesSet".equals(methodName)) {
            afterPropertiesSetFuture = future;
        } else {
            initMethodFuture = future;
        }

        return null;
    }
}
