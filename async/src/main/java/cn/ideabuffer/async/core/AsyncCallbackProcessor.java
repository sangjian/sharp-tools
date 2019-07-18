package cn.ideabuffer.async.core;

import cn.ideabuffer.async.exception.AsyncException;

/**
 * @author sangjian.sj
 * @date 2019/06/28
 */
public class AsyncCallbackProcessor {

    public static <T> void doCallback(AsyncCallback<T> callback, AsyncCallbackContext<T> context) {
        if(callback == null || context == null) {
            return;
        }
        if(context.isSuccess()) {
            callback.onSuccess(context.getResult());
        } else {
            callback.onFailure(context.getThrowable());
        }
    }

}
