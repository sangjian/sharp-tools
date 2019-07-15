package cn.ideabuffer.async.cache;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sangjian.sj
 * @date 2019/07/14
 */
public class AsyncThreadCache {

    private static final Set<Thread> ASYNC_THREAD_SET = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    public static void addAsyncThread(Thread t) {
        if(t != null) {
            ASYNC_THREAD_SET.add(t);
        }
    }

    public static boolean isAsyncThread(Thread t) {
        if(t == null) {
            return false;
        }

        return ASYNC_THREAD_SET.contains(t);
    }

}
