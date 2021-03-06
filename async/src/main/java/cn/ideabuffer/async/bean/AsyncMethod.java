/**
 * Copyright 2018-2118 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ideabuffer.async.bean;

import cn.ideabuffer.async.core.AsyncExecutor;

import java.lang.reflect.Method;

/**
 * 封装异步方法
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncMethod {

    private Object target;

    private Method method;

    private long timeout;

    private String executorName;

    private AsyncExecutor executor;

    private boolean allowThreadLocalTransfer;

    private boolean allowCascade;

    public AsyncMethod(Object target, Method method, long timeout, String executorName,
        AsyncExecutor executor, boolean allowThreadLocalTransfer, boolean allowCascade) {
        this.target = target;
        this.method = method;
        this.timeout = timeout;
        this.executorName = executorName;
        this.executor = executor;
        this.allowThreadLocalTransfer = allowThreadLocalTransfer;
        this.allowCascade = allowCascade;
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public AsyncExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(AsyncExecutor executor) {
        this.executor = executor;
    }

    public boolean isAllowThreadLocalTransfer() {
        return allowThreadLocalTransfer;
    }

    public void setAllowThreadLocalTransfer(boolean allowThreadLocalTransfer) {
        this.allowThreadLocalTransfer = allowThreadLocalTransfer;
    }

    public boolean isAllowCascade() {
        return allowCascade;
    }

    public void setAllowCascade(boolean allowCascade) {
        this.allowCascade = allowCascade;
    }
}
