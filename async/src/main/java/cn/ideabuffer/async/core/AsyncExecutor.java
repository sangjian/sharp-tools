package cn.ideabuffer.async.core;

import java.util.concurrent.*;

/**
 * 异步执行器
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncExecutor {

    private final Object poolSizeMonitor = new Object();

    private int corePoolSize = 16;

    private int maxPoolSize = Integer.MAX_VALUE;

    private int keepAliveSeconds = 60;

    private int queueCapacity = Integer.MAX_VALUE;

    private RejectMode rejectMode = RejectMode.CALLER_RUN;

    private boolean allowCoreThreadTimeOut = false;

    private volatile boolean inited = false;

    private AsyncThreadPool threadPoolExecutor;

    public AsyncExecutor() {
        init();
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int queueCapacity,
        RejectMode rejectMode) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.queueCapacity = queueCapacity;
        this.rejectMode = rejectMode;
        init();
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity,
        RejectMode rejectMode, boolean allowCoreThreadTimeOut) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveSeconds = keepAliveSeconds;
        this.queueCapacity = queueCapacity;
        this.rejectMode = rejectMode;
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        init();
    }

    private void init() {

        if(inited) {
            return;
        }
        BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);

        AsyncThreadPool executor  = new AsyncThreadPool(
            this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
            queue, new AsyncThreadFactory(), getHandler(rejectMode));

        if (this.allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }

        this.threadPoolExecutor = executor;
        inited = true;
    }

    private RejectedExecutionHandler getHandler(RejectMode mode) {
        return RejectMode.CALLER_RUN == mode ? new ThreadPoolExecutor.CallerRunsPolicy()
            : new ThreadPoolExecutor.AbortPolicy();
    }

    public void setCorePoolSize(int corePoolSize) {
        synchronized (this.poolSizeMonitor) {
            this.corePoolSize = corePoolSize;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setCorePoolSize(corePoolSize);
            }
        }
    }

    public int getCorePoolSize() {
        synchronized (this.poolSizeMonitor) {
            return this.corePoolSize;
        }
    }

    public void setMaxPoolSize(int maxPoolSize) {
        synchronized (this.poolSizeMonitor) {
            this.maxPoolSize = maxPoolSize;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
            }
        }
    }

    public int getMaxPoolSize() {
        synchronized (this.poolSizeMonitor) {
            return this.maxPoolSize;
        }
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        synchronized (this.poolSizeMonitor) {
            this.keepAliveSeconds = keepAliveSeconds;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
            }
        }
    }

    public int getKeepAliveSeconds() {
        synchronized (this.poolSizeMonitor) {
            return this.keepAliveSeconds;
        }
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    public void setRejectMode(RejectMode rejectMode) {
        this.rejectMode = rejectMode;
    }

    public RejectMode getRejectMode() {
        return rejectMode;
    }

    public <T> void execute(AsyncCallable<T> callable) {
        submit(callable);
    }

    public <T> void execute(AsyncCallable<T> callable, AsyncCallback<T> callback) {
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> callable) {
        return threadPoolExecutor.submit(callable);
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> callable, AsyncCallback<T> callback) {
        return threadPoolExecutor.submit(callable, callback);
    }

    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingQueue<Runnable>(queueCapacity);
        }
        else {
            return new SynchronousQueue<Runnable>();
        }
    }

    /**
     * 拒绝策略
     */
    enum RejectMode {
        /**
         * 当前线程执行
         */
        CALLER_RUN,
        /**
         * 抛异常
         */
        ABORT
    }
}
