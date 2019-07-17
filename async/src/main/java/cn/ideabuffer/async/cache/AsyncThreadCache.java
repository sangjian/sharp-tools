package cn.ideabuffer.async.cache;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sangjian.sj
 * @date 2019/07/14
 */
public class AsyncThreadCache {

    private static final Set<Long> ASYNC_THREAD_SET = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    public static void addAsyncThread(long threadId) {
        ASYNC_THREAD_SET.add(threadId);
    }

    public static boolean isAsyncThread(long threadId) {
        return ASYNC_THREAD_SET.contains(threadId);
    }

}
