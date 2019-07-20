package cn.ideabuffer.async.core;

import java.util.concurrent.*;

/**
 * 异步线程池
 * @author sangjian.sj
 * @date 2019/06/19
 */
public class AsyncThreadPool extends ThreadPoolExecutor {


    public AsyncThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, AsyncThreadFactory threadFactory,
        RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> task) {
        return submit(task, null);
    }

    public <T> AsyncFutureTask<T> submit(AsyncCallable<T> task, AsyncCallback<T> callback) {
        AsyncFutureTask<T> futureTask = new AsyncFutureTask<>(task, callback);
        execute(futureTask);
        return futureTask;
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }
}
