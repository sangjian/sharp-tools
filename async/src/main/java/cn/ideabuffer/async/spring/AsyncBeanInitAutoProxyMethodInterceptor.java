package cn.ideabuffer.async.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/15
 */
public class AsyncBeanInitAutoProxyMethodInterceptor implements MethodInterceptor {

    private String initMethodName;

    private Future<?> initMethodFuture;

    private Future<?> afterPropertiesSetFuture;

    private ExecutorService beanInitExecutor;

    private volatile boolean inited;

    public AsyncBeanInitAutoProxyMethodInterceptor(String initMethodName, ExecutorService beanInitExecutor) {
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
            if(isInitializingBean) {
                afterPropertiesSetFuture.get();
            }
            if(methodName.equals(initMethodName)) {
                initMethodFuture.get();
            }
            inited = true;
            return invocation.proceed();
        }

        Future<?> future = beanInitExecutor.submit(() -> {
            try {
                invocation.proceed();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        if(isInitializingBean) {
            afterPropertiesSetFuture = future;
        } else {
            initMethodFuture = future;
        }

        return null;
    }
}
