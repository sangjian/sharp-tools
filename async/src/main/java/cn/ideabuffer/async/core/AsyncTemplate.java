package cn.ideabuffer.async.core;

/**
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncTemplate {

    private AsyncExecutor executor;

    public AsyncExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncExecutor executor) {
        this.executor = executor;
    }
}
