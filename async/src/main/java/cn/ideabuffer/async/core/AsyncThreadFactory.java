package cn.ideabuffer.async.core;

import cn.ideabuffer.async.cache.AsyncThreadCache;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public abstract class AsyncThreadFactory implements ThreadFactory {

    @Override
    public final Thread newThread(Runnable r) {
        Thread t = doNewThread(r);
        AsyncThreadCache.addAsyncThread(t.getId());
        return t;
    }

    public abstract Thread doNewThread(Runnable r);
}
