package cn.ideabuffer.async.core;

/**
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncCallbackContext<T> {

    private boolean success;

    private boolean timeout;

    private T result;

    private Throwable throwable;

    public AsyncCallbackContext() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
