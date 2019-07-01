package cn.ideabuffer.async.core;

import cn.ideabuffer.async.proxy.AsyncProxyUtils;
import cn.ideabuffer.async.proxy.AsyncResultProxyBuilder;


/**
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncTemplate {

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

    public <T> T submit(AsyncCallable<T> callable, Class<T> returnClass) {
        return submit(callable, null, returnClass);
    }

    public <T> T submit(AsyncCallable<T> callable, AsyncCallback<T> callback, Class<T> returnClass) {
        if(callable == null || returnClass == null) {
            throw new NullPointerException();
        }
        if(!AsyncProxyUtils.canProxy(returnClass)) {
            try {
                return callable.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        AsyncFutureTask<T> future = executor.submit(callable, callback);

        return (T)new AsyncResultProxyBuilder(future).buildProxy(returnClass);
    }

}
