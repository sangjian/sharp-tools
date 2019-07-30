package cn.ideabuffer.async.core;

import com.taobao.eagleeye.EagleEye;
import com.taobao.eagleeye.RpcContext_inner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 异步线程池
 * @author sangjian.sj
 * @date 2019/06/19
 */
public class AsyncThreadPool extends ThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(AsyncThreadPool.class);

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

        if(!(command instanceof AsyncFutureTask)) {
            super.execute(new Runnable() {
                @Override
                public void run() {
                    ThreadLocalTransmitter.clear(Thread.currentThread());
                    command.run();
                }
            });
            return;
        }

        RpcContext_inner rpcContext = EagleEye.getRpcContext();
        Thread callerThread = Thread.currentThread();

        AsyncFutureTask task = (AsyncFutureTask)command;
        boolean isAllowThreadLocalTransfer = task.isAllowThreadLocalTransfer();

        final boolean allowThreadLocalTransfer = isAllowThreadLocalTransfer;

        Object callerThreadLocalMap = null;
        Object callerInheritableThreadLocalMap = null;
        if(allowThreadLocalTransfer) {
            // 调用线程ThreadLocalMap
            callerThreadLocalMap = ThreadLocalTransmitter.getThreadLocalMap(callerThread);
            // 调用线程InheritableThreadLocalMap
            callerInheritableThreadLocalMap = ThreadLocalTransmitter.getInheritableThreadLocalMap(callerThread);

        }

        final Object finalCallerThreadLocalMap = callerThreadLocalMap;
        final Object finalCallerInheritableThreadLocalMap = callerInheritableThreadLocalMap;


        super.execute(new CallerRunDecoratedTask(){
            @Override
            public void run() {
                Thread runnerThread = Thread.currentThread();
                boolean isCallerRun = this.isCallerRun();

                try {
                    // 复制ThreadLocal
                    if(allowThreadLocalTransfer && !isCallerRun && finalCallerThreadLocalMap != null && finalCallerInheritableThreadLocalMap != null) {
                        logger.info("in setThreadLocal, callerThread:{}, finalCallerThreadLocalMap:{}", callerThread.getName(), finalCallerThreadLocalMap);
                        ThreadLocalTransmitter.setThreadLocalMap(finalCallerThreadLocalMap, runnerThread);
                        ThreadLocalTransmitter.setInheritableThreadLocalMap(finalCallerInheritableThreadLocalMap, runnerThread);
                    }

                    if(!isCallerRun) {
                        EagleEye.setRpcContext(rpcContext);
                    }

                    command.run();
                } finally {

                    // 清理ThreadLocal
                    if(allowThreadLocalTransfer && !isCallerRun) {
                        logger.info("in threadlocal clear");
                        ThreadLocalTransmitter.clear(runnerThread);
                        // 清理 ThreadLocal 的上下文，避免异步线程复用时出现上下文互串的问题
                        // 如果执行了callerRun，则不清除
                    } else if(!isCallerRun) {
                        logger.info("in clearRpcContext, this:{}");
                        EagleEye.clearRpcContext();
                    }
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
