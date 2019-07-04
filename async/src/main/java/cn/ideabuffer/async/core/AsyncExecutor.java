package cn.ideabuffer.async.core;

import java.util.concurrent.*;

/**
 * 异步执行器
 *
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncExecutor {

    private static final int DEFAULT_KEEP_ALIVE_SECONDES = 60;

    private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int DEFAULT_MAX_POOL_SIZE = 2 * DEFAULT_CORE_POOL_SIZE;

    private static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;

    private final Object poolSizeMonitor = new Object();

    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;

    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDES;

    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;

    private RejectMode rejectMode = RejectMode.CALLER_RUN;

    private boolean allowCoreThreadTimeOut = false;

    private volatile boolean inited = false;

    private AsyncThreadPool threadPoolExecutor;

    public AsyncExecutor() {
    }

    public AsyncExecutor(int nThreads) {
        this(nThreads, nThreads);
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize) {
        this(corePoolSize, maxPoolSize, DEFAULT_QUEUE_CAPACITY);
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int queueCapacity) {
        this(corePoolSize, maxPoolSize, queueCapacity, DEFAULT_KEEP_ALIVE_SECONDES);
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds) {
        this(corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds, RejectMode.CALLER_RUN);
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds,
        RejectMode rejectMode) {
        this(corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds, rejectMode, false);
    }

    public AsyncExecutor(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity,
        RejectMode rejectMode, boolean allowCoreThreadTimeOut) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveSeconds = keepAliveSeconds;
        this.queueCapacity = queueCapacity;
        this.rejectMode = rejectMode;
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    public void init() {

        if (inited) {
            return;
        }
        BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);

        AsyncThreadPool executor = new AsyncThreadPool(
            this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
            queue, new AsyncThreadFactory(), getHandler(rejectMode));

        if (this.allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }

        this.threadPoolExecutor = executor;
        inited = true;
    }

    public void destroy() {
        if(!inited) {
            return;
        }
        inited = false;
        threadPoolExecutor.shutdown();
    }

    private RejectedExecutionHandler getHandler(RejectMode mode) {
        return RejectMode.CALLER_RUN == mode ? new ThreadPoolExecutor.CallerRunsPolicy()
            : new ThreadPoolExecutor.AbortPolicy();
    }

    public int getCorePoolSize() {
        synchronized (this.poolSizeMonitor) {
            return this.corePoolSize;
        }
    }

    public void setCorePoolSize(int corePoolSize) {
        synchronized (this.poolSizeMonitor) {
            this.corePoolSize = corePoolSize;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setCorePoolSize(corePoolSize);
            }
        }
    }

    public int getMaxPoolSize() {
        synchronized (this.poolSizeMonitor) {
            return this.maxPoolSize;
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

    public int getKeepAliveSeconds() {
        synchronized (this.poolSizeMonitor) {
            return this.keepAliveSeconds;
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

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
    }

    public RejectMode getRejectMode() {
        return rejectMode;
    }

    public void setRejectMode(RejectMode rejectMode) {
        this.rejectMode = rejectMode;
    }

    public void execute(Runnable task) {
        execute(task, 0);
    }

    public void execute(Runnable task, long timeout) {
        execute(task, timeout, null);
    }

    public void execute(Runnable task, long timeout, AsyncCallback<Void> callback) {
        AsyncCallable<Void> callable = new AsyncCallable<Void>() {
            @Override
            public Void call() throws Exception {
                task.run();
                return null;
            }

            @Override
            public long getTimeout() {
                return timeout;
            }
        };
        execute(callable, callback);
    }

    public <T> void execute(AsyncCallable<T> task) {
        submit(task);
    }

    public <T> void execute(AsyncCallable<T> task, AsyncCallback<T> callback) {
        submit(task, callback);
    }

    public <T> AsyncFutureTask<T> submit(Callable<T> task) {
        return submit(task, 0);
    }

    public <T> AsyncFutureTask<T> submit(Callable<T> task, long timeout) {
        return submit(task, timeout, null);
    }

    public <T> AsyncFutureTask<T> submit(Callable<T> task, long timeout, AsyncCallback<T> callback) {
        if(task instanceof AsyncCallable) {
            return submit((AsyncCallable<T>)task);
        }
        return submit(new AsyncCallable<T>() {
            @Override
            public T call() throws Exception {
                return task.call();
            }

            @Override
            public long getTimeout() {
                return timeout;
            }
        }, callback);
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> task) {
        return threadPoolExecutor.submit(task);
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> task, AsyncCallback<T> callback) {
        return threadPoolExecutor.submit(task, callback);
    }

    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity > 0) {
            return new LinkedBlockingQueue<>(queueCapacity);
        } else {
            return new SynchronousQueue<>();
        }
    }

    public boolean isShutdown() {
        return !inited;
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
