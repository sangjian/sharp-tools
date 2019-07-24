package cn.ideabuffer.async.core;

import com.taobao.eagleeye.EagleEye;
import com.taobao.eagleeye.RpcContext_inner;

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
        RpcContext_inner rpcContext = EagleEye.getRpcContext();
        Thread callerThread = Thread.currentThread();
        boolean isAllowThreadLocalTransfer = false;
        if(command instanceof AsyncFutureTask) {
            AsyncFutureTask task = (AsyncFutureTask)command;
            isAllowThreadLocalTransfer = task.isAllowThreadLocalTransfer();
        }

        final boolean allowThreadLocalTransfer = isAllowThreadLocalTransfer;

        super.execute(() -> {
            Thread runnerThread = Thread.currentThread();
            try {
                if(callerThread != runnerThread && rpcContext != null) {
                    EagleEye.setRpcContext(rpcContext);
                }
                // 复制ThreadLocal
                if(allowThreadLocalTransfer && callerThread != runnerThread) {
                    // 调用线程ThreadLocalMap
                    Object callerThreadLocalMap = ThreadLocalTransmitter.getThreadLocalMap(callerThread);
                    // 调用线程InheritableThreadLocalMap
                    Object callerInheritableThreadLocalMap = ThreadLocalTransmitter.getInheritableThreadLocalMap(callerThread);
                    ThreadLocalTransmitter.setThreadLocalMap(callerThreadLocalMap, runnerThread);
                    ThreadLocalTransmitter.setInheritableThreadLocalMap(callerInheritableThreadLocalMap, runnerThread);
                }

                command.run();
            } finally {
                // 务必清理 ThreadLocal 的上下文，避免异步线程复用时出现上下文互串的问题
                // 如果执行了callerRun，则不清除
                if(callerThread != runnerThread) {
                    EagleEye.clearRpcContext();
                }
                // 清理ThreadLocal
                if(allowThreadLocalTransfer && callerThread != runnerThread) {
                    ThreadLocalTransmitter.clear(runnerThread);
                }
            }

        });
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
