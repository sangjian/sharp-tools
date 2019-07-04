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
import org.springframework.util.ReflectionUtils;

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

        if (asyncMethod == null || (
            !AsyncProxyUtils.canProxy(method.getReturnType()))
            && !AsyncProxyUtils.isVoid(method.getReturnType())) {
            return methodProxy.invokeSuper(obj, args);
        }


        AsyncExecutor executor = asyncMethod.getExecutor();

        if(executor == null && !SpringApplicationContextHolder.containsBean(asyncMethod.getExecutorName())) {
            throw new AsyncException(String.format("executor[%s] not found!", asyncMethod.getExecutorName()));
        }

        executor = SpringApplicationContextHolder.getBean(asyncMethod.getExecutorName(), AsyncExecutor.class);
        asyncMethod.setExecutor(executor);

        // 如果线程池销毁，则直接执行
        if(executor.isShutdown()) {
            return methodProxy.invokeSuper(obj, args);
        }

        final Object[] finArgs = args;

        AsyncFutureTask<Object> future = executor.submit(new AsyncCallable<Object>() {

            @Override
            public Object call() {
                try {
                    return methodProxy.invokeSuper(obj, finArgs);
                } catch (Throwable throwable) {
                    throw new AsyncException(String.format("task method:[%s#%s] invoke failed!", method.getDeclaringClass().getName(), method.getName()), throwable);
                }
            }

            @Override
            public long getTimeout() {
                return asyncMethod.getTimeout();
            }
        });
        if(AsyncProxyUtils.isVoid(method.getReturnType())) {
            return null;
        }
        return new AsyncResultProxyBuilder(future).buildProxy(method.getReturnType());
    }

}
