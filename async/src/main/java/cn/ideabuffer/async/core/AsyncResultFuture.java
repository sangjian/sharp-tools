package cn.ideabuffer.async.core;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author sangjian.sj
 * @date 2019/07/03
 */
public class AsyncResultFuture<T> implements Future<T> {

    private final T result;

    public AsyncResultFuture(T result) {
        this.result = result;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() {
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) {
        return result;
    }

    public static <T> Future<T> forValue(T value) {
        return new AsyncResultFuture<>(value);
    }
}
