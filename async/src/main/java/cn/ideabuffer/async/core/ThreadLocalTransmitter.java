package cn.ideabuffer.async.core;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author sangjian.sj
 * @date 2019/06/29
 */
public class ThreadLocalTransmitter {

    private static final Field THREAD_LOCAL_FIELD = ReflectionUtils.findField(Thread.class, "threadLocals");
    private static final Field INHERITABLE_THREAD_LOCAL_FIELD = ReflectionUtils.findField(Thread.class,
        "inheritableThreadLocals");

    static {
        ReflectionUtils.makeAccessible(THREAD_LOCAL_FIELD);
        ReflectionUtils.makeAccessible(INHERITABLE_THREAD_LOCAL_FIELD);
    }

    public static Object getThreadLocalMap(Thread thread) {
        return ReflectionUtils.getField(THREAD_LOCAL_FIELD, thread);
    }

    public static Object getInheritableThreadLocalMap(Thread thread) {
        return ReflectionUtils.getField(INHERITABLE_THREAD_LOCAL_FIELD, thread);
    }

    public static void setThreadLocalMap(Object threadLocalMap, Thread thread) {
        if (threadLocalMap != null) {
            ReflectionUtils.setField(THREAD_LOCAL_FIELD, thread, threadLocalMap);
        }
    }

    public static void setInheritableThreadLocalMap(Object inheritableThreadLocalMap, Thread thread) {
        if (inheritableThreadLocalMap != null) {
            ReflectionUtils.setField(INHERITABLE_THREAD_LOCAL_FIELD, thread, inheritableThreadLocalMap);
        }
    }

    public static void copy(Thread caller, Thread runner) {
        if (caller == null || runner == null) {
            return;
        }
        synchronized (caller) {
            Object callerThreadLocalMap = ReflectionUtils.getField(THREAD_LOCAL_FIELD, caller);
            if (callerThreadLocalMap != null) {
                ReflectionUtils.setField(THREAD_LOCAL_FIELD, runner, callerThreadLocalMap);
            }

            Object callerInheritableThreadLocalMap = ReflectionUtils.getField(INHERITABLE_THREAD_LOCAL_FIELD, caller);
            if (callerInheritableThreadLocalMap != null) {
                ReflectionUtils.setField(INHERITABLE_THREAD_LOCAL_FIELD, runner, callerInheritableThreadLocalMap);
            }
        }
    }

    public static void clear(Thread runner) {
        if (runner == null) {
            return;
        }

        ReflectionUtils.setField(THREAD_LOCAL_FIELD, runner, null);
        ReflectionUtils.setField(INHERITABLE_THREAD_LOCAL_FIELD, runner, null);
    }
}
