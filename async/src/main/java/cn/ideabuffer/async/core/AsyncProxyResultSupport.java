package cn.ideabuffer.async.core;

/**
 * 代理对象结果接口
 * @author sangjian.sj
 * @date 2019/06/24
 */
public interface AsyncProxyResultSupport {

    /**
     * 是否为null
     * @return
     */
    boolean _isNull();

    /**
     * 获取真实对象
     * @return
     */
    Object _getResult();

    /**
     * 获取异步任务的Future对象
     * @return
     */
    AsyncFutureTask<?> _getFuture();

}
