package cn.ideabuffer.async.core;

import cn.ideabuffer.async.exception.AsyncException;

/**
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncCallbackProcessor {

    public static <T> void doCallback(AsyncCallback<T> callback, AsyncCallbackContext<T> context) {
        if(callback == null) {
            return;
        }
        if(context == null) {
            throw new AsyncException();
        }

        if(context.isSuccess()) {
            callback.onSuccess(context.getResult());
        } else if (context.isTimeout()) {
            callback.onTimeout(context.getResult());
        } else {
            callback.onFailure(context.getThrowable());
        }
    }

}
