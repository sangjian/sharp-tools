package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.bean.AsyncMethod;
import cn.ideabuffer.async.core.AsyncCallable;
import cn.ideabuffer.async.core.AsyncExecutor;
import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.exception.AsyncException;
import cn.ideabuffer.async.util.SpringApplicationContextHolder;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncMethodInterceptor implements MethodInterceptor {

    private final static Logger logger = LoggerFactory.getLogger(AsyncMethodInterceptor.class);

    private Object targetObject;

    public AsyncMethodInterceptor(Object targetObject) {

        this.targetObject = targetObject;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        final String methodKey = AsyncProxyUtils.genMethodKey(targetObject, method);

        final AsyncMethod asyncMethod = AsyncProxyCache.getProxyMethod(methodKey);

        if (asyncMethod == null || !AsyncProxyUtils.canProxy(method.getReturnType())) {
            return methodProxy.invokeSuper(obj, args);
        }


        AsyncExecutor executor = asyncMethod.getExecutor();

        if(executor == null) {
            executor = SpringApplicationContextHolder.getBean(asyncMethod.getExecutorName(), AsyncExecutor.class);
        }

        if(executor == null) {
            throw new AsyncException(String.format("executor[%s] not found!", asyncMethod.getExecutorName()));
        }
        asyncMethod.setExecutor(executor);
        // TODO 线程池销毁时，需要判断，如果线程池销毁，则直接执行

        final Object[] finArgs = args;

        AsyncFutureTask<Object> future = executor.submit(new AsyncCallable<Object>() {

            @Override
            public Object call() {
                try {
                    Object result =  methodProxy.invokeSuper(obj, finArgs);
                    if(result == null) {
                        return null;
                    }
                    if(result instanceof Future) {
                        return ((Future<?>)result).get();
                    }
                    return result;
                } catch (Throwable e) {
                    throw new AsyncException(e);
                }
            }

            @Override
            public long getTimeout() {
                return asyncMethod.getTimeout();
            }
        });

        return new AsyncResultProxyBuilder(future).buildProxy(method.getReturnType());
    }

}
