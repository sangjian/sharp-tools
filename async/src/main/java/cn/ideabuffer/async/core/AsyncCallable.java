package cn.ideabuffer.async.core;

import java.util.concurrent.Callable;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public interface AsyncCallable<T> extends Callable<T> {
    long getTimeout();
}
