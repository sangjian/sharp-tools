package cn.ideabuffer.async.core;

import cn.ideabuffer.async.proxy.AsyncProxyUtils;
import cn.ideabuffer.async.proxy.AsyncResultProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 异步执行模板
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncTemplate {

    private final static Logger logger = LoggerFactory.getLogger(AsyncTemplate.class);

    private AsyncExecutor executor;

    public AsyncTemplate() {
    }

    public AsyncTemplate(AsyncExecutor executor) {
        this.executor = executor;
    }

    public AsyncExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncExecutor executor) {
        this.executor = executor;
    }

    public <T> T submit(Callable<T> task, Class<T> returnClass) {
        return submit(task, 0, returnClass);
    }

    public <T> T submit(Callable<T> task, long timeout, Class<T> returnClass) {
        return submit(task, timeout, null, returnClass);
    }

    public <T> T submit(Callable<T> task, long timeout, AsyncCallback<T> callback, Class<T> returnClass) {
        if(task == null || returnClass == null) {
            logger.error("task or returnClass is null");
            throw new IllegalArgumentException("task or returnClass is null");
        }
        if(task instanceof AsyncCallable) {
            return submit((AsyncCallable<T>)task, callback, returnClass);
        }
        AsyncCallable<T> callable = new AsyncCallable<T>() {
            @Override
            public T call() throws Exception {
                return task.call();
            }

            @Override
            public long getTimeout() {
                return timeout;
            }
        };
        return submit(callable, callback, returnClass);
    }

    public <T> T submit(AsyncCallable<T> task, Class<T> returnClass) {
        return submit(task, null, returnClass);
    }

    public <T> T submit(AsyncCallable<T> task, AsyncCallback<T> callback, Class<T> returnClass) {
        if(task == null || returnClass == null) {
            throw new NullPointerException();
        }
        if(!AsyncProxyUtils.canProxy(returnClass)
            && !AsyncProxyUtils.isVoid(returnClass)) {
            try {
                return task.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncFutureTask<T> future = executor.submit(task, callback);

        if(AsyncProxyUtils.isVoid(returnClass)) {
            return null;
        }

        return (T)new AsyncResultProxyBuilder(future).buildProxy(returnClass);
    }

    public void execute(Runnable task) {
        execute(task, 0);
    }

    public void execute(Runnable task, long timeout) {
        execute(task, timeout, null);
    }

    public void execute(Runnable task, long timeout, AsyncCallback<Void> callback) {
        if(task == null) {
            throw new NullPointerException("task cannot be null");
        }
        executor.execute(task, timeout, callback);
    }

}
