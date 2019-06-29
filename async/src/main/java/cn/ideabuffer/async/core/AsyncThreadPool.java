package cn.ideabuffer.async.core;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.*;

/**
 * @author sangjian.sj
 * @date 2019/06/19
 */
public class AsyncThreadPool extends ThreadPoolExecutor {

    private static final Field THREAD_LOCAL_FIELD = ReflectionUtils.findField(Thread.class, "threadLocals");
    private static final Field INHERITABLE_THREAD_LOCAL_FIELD = ReflectionUtils.findField(Thread.class,
        "inheritableThreadLocals");
    static {
        // 强制的声明accessible
        ReflectionUtils.makeAccessible(THREAD_LOCAL_FIELD);
        ReflectionUtils.makeAccessible(INHERITABLE_THREAD_LOCAL_FIELD);
    }


    public AsyncThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public AsyncThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
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
        if(command instanceof AsyncFutureTask) {
            Thread thread = Thread.currentThread();
            if (ReflectionUtils.getField(THREAD_LOCAL_FIELD, thread) == null) {
                // 创建一个空的ThreadLocal,立马写回去
                new ThreadLocal<Boolean>(); // 这时会在runner线程产生一空记录的ThreadLocalMap记录
            }
            if (ReflectionUtils.getField(INHERITABLE_THREAD_LOCAL_FIELD, thread) == null) {
                // 创建一个空的ThreadLocal,立马写回去
                new InheritableThreadLocal<Boolean>(); // 可继承的ThreadLocal
            }
        }
        super.execute(command);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        // 在执行之前处理下ThreadPool的属性继承
        if (r instanceof AsyncFutureTask) {
            AsyncFutureTask futureTask = (AsyncFutureTask) r;
            initThreadLocal(THREAD_LOCAL_FIELD, futureTask.getCallerThread(), t);
            initThreadLocal(INHERITABLE_THREAD_LOCAL_FIELD, futureTask.getCallerThread(), t);
        }

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
    }

    private void initThreadLocal(Field field, Thread caller, Thread runner) {
        if (caller == null || runner == null) {
            return;
        }
        // 主要考虑这样的情况：
        // 1.
        // 如果caller线程没有使用ThreadLocal对象，而异步加载的runner线程执行中使用了ThreadLocal对象，则需要复制对象到caller线程上
        // 2.
        // 后续caller,多个runner线程有使用ThreadLocal对象，使用的是同一个引用,直接set都是针对同一个ThreadLocal,所以以后就不需要进行合并

        // 因为在提交Runnable时已经同步创建了一个ThreadLocalMap对象，所以runner线程只需要复制caller对应的引用即可，不需要进行合并，简化处理
        // threadlocal属性复制,注意是引用复制
        Object callerThreadLocalMap = ReflectionUtils.getField(field, caller);
        if (callerThreadLocalMap != null) {
            ReflectionUtils.setField(field, runner, callerThreadLocalMap);// 复制caller的信息到runner线程上
        } else {
            // 这个分支不会出现,因为在execute提交的时候已经添加
        }
    }

    private void recoverThreadLocal(Field field, Thread caller, Thread runner) {
        if (runner == null) {
            return;
        }
        // 清理runner线程的ThreadLocal，为下一个task服务
        ReflectionUtils.setField(field, runner, null);
    }
}
