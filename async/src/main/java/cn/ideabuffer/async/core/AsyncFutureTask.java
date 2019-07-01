package cn.ideabuffer.async.core;

import cn.ideabuffer.async.bean.AsyncMethod;
import cn.ideabuffer.async.exception.AsyncException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

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
    private Thread runnerThread;

    private AsyncCallbackContext<T> callbackContext;

    public AsyncFutureTask(AsyncCallable<T> callable) {
        this(callable, null);
    }

    public AsyncFutureTask(AsyncCallable<T> callable, AsyncCallback<T> callback) {
        super(callable);
        this.timeout = callable.getTimeout();
        this.callerThread = Thread.currentThread();
        if (callback != null) {
            this.callback = callback;
        }
    }

    @Override
    protected void done() {
        endTime = System.currentTimeMillis();
        if(callback == null) {
            return;
        }
        AsyncCallbackProcessor.doCallback(callback, callbackContext);
    }

    @Override
    public void run() {
        super.run();
    }

    public void syncRun() {
        run();
    }

    public T getValue() {
        if (value != null) {
            return value;
        }
        startTime = System.currentTimeMillis();
        if(shouldCallback()) {
            callbackContext = new AsyncCallbackContext<>();
            callbackContext.setSuccess(false);
        }
        Throwable throwable = null;
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
        if(throwable != null) {
            if(shouldCallback()) {
                callbackContext.setThrowable(throwable);
            }
            if(throwable instanceof TimeoutException) {
                callbackContext.setTimeout(true);
            }
            throw new AsyncException(throwable);
        }
        if(shouldCallback()) {
            callbackContext.setSuccess(true);
            callbackContext.setResult(value);
        }


        return value;
    }

    private boolean shouldCallback() {
        return callback != null;
    }

    public Thread getCallerThread() {
        return callerThread;
    }

    public void setCallerThread(Thread callerThread) {
        this.callerThread = callerThread;
    }

    public Thread getRunnerThread() {
        return runnerThread;
    }

    public void setRunnerThread(Thread runnerThread) {
        this.runnerThread = runnerThread;
    }
}
