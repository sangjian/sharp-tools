package cn.ideabuffer.async.core;

import java.util.concurrent.Callable;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public interface AsyncCallback<T> {
    /**
     * 执行成功回调方法
     */
    void onSuccess(T result);

    /**
     * 执行失败回调方法
     */
    void onFailure(Throwable t);

    /**
     * 执行超时回调方法
     * @param result
     */
    void onTimeout(T result);
}
