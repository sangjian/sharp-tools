package cn.ideabuffer.async.core;

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

}
