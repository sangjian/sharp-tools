package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.bean.AsyncMethod;
import cn.ideabuffer.async.core.AsyncCallable;
import cn.ideabuffer.async.core.AsyncExecutor;
import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.core.AsyncProxyResultSupport;
import cn.ideabuffer.async.exception.AsyncException;
import cn.ideabuffer.async.util.SpringApplicationContextHolder;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

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
            return method.invoke(targetObject, args);
        }

        AsyncExecutor executor = asyncMethod.getExecutor();

        if (executor == null && !SpringApplicationContextHolder.containsBean(asyncMethod.getExecutorName())) {
            throw new AsyncException(String.format("executor[%s] not found!", asyncMethod.getExecutorName()));
        }

        executor = SpringApplicationContextHolder.getBean(asyncMethod.getExecutorName(), AsyncExecutor.class);
        asyncMethod.setExecutor(executor);

        // 如果线程池销毁，则直接执行
        if (executor.isShutdown()) {
            return method.invoke(targetObject, args);
        }

        final Object[] finArgs = args;

        // 先创建Future，然后创建代理对象；
        // 否则如果开启了ThreadLocal复制，会出现ThreadLocal被覆盖，导致task对应的Future不正确，出现死锁的情况
        AsyncFutureTask<Object> future = new AsyncFutureTask<>(new AsyncCallable<Object>() {

            @Override
            public Object call() {
                try {
                    Object result = methodProxy.invokeSuper(obj, finArgs);
                    // 如果返回的结果是代理对象，需要等待代理对象对应的任务执行完
                    if (result instanceof AsyncProxyResultSupport) {
                        AsyncFutureTask futureTask = ((AsyncProxyResultSupport)result)._getFuture();
                        // 重要，如果不判断任务是否已经执行，那么可能会出现死锁的情况，具体如下
                        // 1. 当前线程调用futureTask.getValue()阻塞；
                        // 2. 此时线程池核心线程已满，futureTask对应的task在队列中；
                        // 3. 这时futureTask.getValue()会等待对应的task执行完，但task可能永远不会执行，
                        //    因为当前任务占用了一个线程，如果核心线程都在等待队列中的任务，则队列中的任务永远都没有可用线程来执行
                        if (futureTask.getRunnerThread() == null) {
                            Thread.yield();
                        } else {
                            result = futureTask.getValue();
                        }
                    }

                    return result;
                } catch (Throwable throwable) {
                    throw new AsyncException(String
                        .format("task method:[%s#%s] invoke failed!", method.getDeclaringClass().getName(),
                            method.getName()), throwable);
                }
            }

            @Override
            public long getTimeout() {
                return asyncMethod.getTimeout();
            }
        });

        if (AsyncProxyUtils.isVoid(method.getReturnType())) {
            executor.execute(future);
            return null;
        }
        Object result = new AsyncResultProxyBuilder(future).buildProxy(method.getReturnType());
        executor.execute(future);
        return result;
    }

}
