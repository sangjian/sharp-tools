package cn.ideabuffer.async.core;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.*;

/**
 * 异步执行器
 *
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncExecutor implements InitializingBean, DisposableBean {

    private static final int DEFAULT_KEEP_ALIVE_SECONDES = 60;

    private static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    private static final int DEFAULT_MAX_POOL_SIZE = 2 * Runtime.getRuntime().availableProcessors();

    private static final int DEFAULT_QUEUE_CAPACITY = 64;

    private final Object poolSizeMonitor = new Object();

    private int corePoolSize = DEFAULT_CORE_POOL_SIZE;

    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDES;

    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;

    private RejectMode rejectMode = RejectMode.CALLER_RUN;

    private boolean allowCoreThreadTimeOut = true;

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
            queue, new DefaultAsyncThreadFactory(), getHandler(rejectMode));

        if (this.allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }

        this.threadPoolExecutor = executor;
        inited = true;
    }

    @Override
    public void afterPropertiesSet() {
        init();
    }

    @Override
    public void destroy() {
        if(!inited) {
            return;
        }
        inited = false;
        threadPoolExecutor.shutdown();
    }

    private RejectedExecutionHandler getHandler(RejectMode mode) {
        return RejectMode.CALLER_RUN == mode ? new CallerRunsPolicy()
            : new ThreadPoolExecutor.AbortPolicy();
    }


    public void resetCorePoolSize(int corePoolSize) {
        synchronized (this.poolSizeMonitor) {
            this.corePoolSize = corePoolSize;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setCorePoolSize(corePoolSize);
            }
        }
    }


    public void resetMaxPoolSize(int maxPoolSize) {
        synchronized (this.poolSizeMonitor) {
            this.maxPoolSize = maxPoolSize;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
            }
        }
    }

    public void resetKeepAliveSeconds(int keepAliveSeconds) {
        synchronized (this.poolSizeMonitor) {
            this.keepAliveSeconds = keepAliveSeconds;
            if (this.threadPoolExecutor != null) {
                this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
            }
        }
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
        return submit(task, null);
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

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public RejectMode getRejectMode() {
        return rejectMode;
    }

    public void setRejectMode(RejectMode rejectMode) {
        this.rejectMode = rejectMode;
    }

    public boolean isAllowCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
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

    /**
     * 获取jvm内存使用率
     * @return
     */
    public static double getMemoryUsage() {
        return (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().maxMemory();
    }

    public AsyncThreadPool getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() { }

        /**
         * Executes task r in the caller's thread, unless the executor
         * has been shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                System.out.println(String.format("time%d\t in rejectedExecution\t\t thread:%s", System.currentTimeMillis(), Thread.currentThread().getName()));
                r.run();
            }
        }
    }
}
