package cn.ideabuffer.async.core;

import cn.ideabuffer.async.exception.AsyncException;
import cn.ideabuffer.async.exception.AsyncExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncFutureTask<T> extends FutureTask<T> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncFutureTask.class);

    private long startTime = 0;

    private long endTime = 0;

    private volatile T value;

    private AsyncCallback<T> callback;

    private long timeout;

    /**
     * 调用者的线程
     */
    private Thread callerThread;

    /**
     * 线程池运行中的线程
     */
    private volatile Thread runnerThread;

    private AsyncCallbackContext<T> callbackContext;

    private AsyncExceptionHandler exceptionHandler;

    /**
     * 是否允许ThreadLocal复制
     */
    private boolean allowThreadLocalTransfer;

    private Method method;

    private Object[] params;

    public AsyncFutureTask(AsyncCallable<T> callable) {
        this(callable, false, null);
    }

    public AsyncFutureTask(AsyncCallable<T> callable, boolean allowThreadLocalTransfer) {
        this(callable, allowThreadLocalTransfer, null);
    }

    public AsyncFutureTask(AsyncCallable<T> callable, AsyncCallback<T> callback) {
        this(callable, false, callback);
    }

    public AsyncFutureTask(AsyncCallable<T> callable, boolean allowThreadLocalTransfer, AsyncCallback<T> callback) {
        super(callable);
        this.timeout = callable.getTimeout();
        this.callerThread = Thread.currentThread();
        this.allowThreadLocalTransfer = allowThreadLocalTransfer;
        if (callback != null) {
            this.callback = callback;
        }
    }

    @Override
    protected void done() {

        if(shouldCallback()) {
            try {
                getValue();
            } catch (Exception e) {
                // ignore
            }
            AsyncCallbackProcessor.doCallback(callback, callbackContext);
        }


    }

    @Override
    public void run() {
        super.run();
    }

    public T getValue() throws Exception {
        startTime = System.currentTimeMillis();
        if (shouldCallback()) {
            callbackContext = new AsyncCallbackContext<T>();
            callbackContext.setSuccess(false);
        }
        Exception throwable = null;
        try {

            if (timeout <= 0) {
                value = super.get();
            } else {
                value = super.get(timeout, TimeUnit.MILLISECONDS);
            }
            endTime = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("invoking load time:{} timeout:{}, value:{}", this.endTime - this.startTime, timeout, value);
            }
        } catch (TimeoutException e) {
            super.cancel(true);
            throwable = e;
        } catch (Exception e) {
            throwable = e;
        }

        if(shouldCallback()) {
            if(throwable != null) {
                callbackContext.setThrowable(throwable);
            } else {
                callbackContext.setSuccess(true);
                callbackContext.setResult(value);
            }
        }
        if(throwable != null) {
            if(this.exceptionHandler != null) {
                this.exceptionHandler.handleException(throwable, method, params);
            } else {
                logger.error("getValue encountered problem!", throwable);
                throw new AsyncException("getValue encountered problem!", throwable);
            }
        }

        return value;
    }

    private boolean shouldCallback() {
        return callback != null;
    }

    public Thread getCallerThread() {
        return callerThread;
    }

    public Thread getRunnerThread() {
        return runnerThread;
    }

    public boolean isAllowThreadLocalTransfer() {
        return allowThreadLocalTransfer;
    }

    public void setAllowThreadLocalTransfer(boolean allowThreadLocalTransfer) {
        this.allowThreadLocalTransfer = allowThreadLocalTransfer;
    }

    public AsyncExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(AsyncExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
