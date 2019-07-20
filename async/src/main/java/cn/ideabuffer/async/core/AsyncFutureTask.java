package cn.ideabuffer.async.core;

import cn.ideabuffer.async.exception.AsyncException;
import com.taobao.eagleeye.EagleEye;
import com.taobao.eagleeye.RpcContext_inner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Thread runnerThread;

    private AsyncCallbackContext<T> callbackContext;

    /**
     * 是否允许ThreadLocal复制
     */
    private boolean allowThreadLocalTransfer;

    /**
     * Eagleeye上下文
     */
    private RpcContext_inner rpcContext;

    /**
     * 调用线程ThreadLocalMap
     */
    private Object callerThreadLocalMap;

    /**
     * 调用线程InheritableThreadLocalMap
     */
    private Object callerInheritableThreadLocalMap;

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
        // 从当前 ThreadLocal 备份
        this.rpcContext = EagleEye.getRpcContext();

        if(this.allowThreadLocalTransfer) {
            callerThreadLocalMap = ThreadLocalTransmitter.getThreadLocalMap(this.callerThread);
            callerInheritableThreadLocalMap = ThreadLocalTransmitter.getInheritableThreadLocalMap(this.callerThread);
        }
    }

    @Override
    protected void done() {
        // 务必清理 ThreadLocal 的上下文，避免异步线程复用时出现上下文互串的问题
        EagleEye.clearRpcContext();

        if(this.allowThreadLocalTransfer) {
            ThreadLocalTransmitter.clear(this.runnerThread);
        }

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
        this.runnerThread = Thread.currentThread();
        // 还原到 ThreadLocal
        EagleEye.setRpcContext(rpcContext);
        if(this.allowThreadLocalTransfer) {
            ThreadLocalTransmitter.setThreadLocalMap(this.callerThreadLocalMap, this.runnerThread);
            ThreadLocalTransmitter.setInheritableThreadLocalMap(this.callerInheritableThreadLocalMap, this.runnerThread);
        }
        super.run();
    }

    public T getValue() {
        startTime = System.currentTimeMillis();
        if (shouldCallback()) {
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

        if(shouldCallback()) {
            if(throwable != null) {
                callbackContext.setThrowable(throwable);
            } else {
                callbackContext.setSuccess(true);
                callbackContext.setResult(value);
            }
        }
        if(throwable != null) {
            logger.error("getValue encountered problem!", throwable);
            throw new AsyncException("getValue encountered problem!", throwable);
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
}
