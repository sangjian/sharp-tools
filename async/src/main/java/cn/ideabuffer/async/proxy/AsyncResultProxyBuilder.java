package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.cache.AsyncProxyCache;
import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.core.AsyncProxyResultSupport;
import cn.ideabuffer.async.exception.AsyncException;
import cn.ideabuffer.async.util.AsyncProxyUtils;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * 结果代理对象builder
 *
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncResultProxyBuilder {

    private final static Logger logger = LoggerFactory.getLogger(AsyncResultProxyBuilder.class);

    public static Object buildProxy(Class<?> target, AsyncFutureTask<?> future) {

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(target.getName());
        if (proxyClass == null) {
            synchronized (AsyncResultProxyBuilder.class) {
                proxyClass = AsyncProxyCache.getProxyClass(target.getName());
                if (proxyClass == null) {
                    Enhancer enhancer = new Enhancer();
                    if (target.isInterface()) {
                        enhancer.setInterfaces(
                            new Class[] {AsyncProxyResultSupport.class, target, AsyncSerializable.class});
                    } else {
                        enhancer.setInterfaces(new Class[] {AsyncProxyResultSupport.class, AsyncSerializable.class});
                        enhancer.setSuperclass(target);
                    }

                    enhancer.setCallbackFilter(new AsyncResultCallbackFilter());
                    enhancer.setCallbackTypes(
                        new Class[] {AsyncResultInterceptor.class, AsyncProxyResultInterceptor.class,
                            AsyncProxySerializeInterceptor.class, AsyncToStringMethodInterceptor.class});
                    proxyClass = enhancer.createClass();
                    logger.info("create result proxy class:{}, proxyClass:{}", target, proxyClass);
                    AsyncProxyCache.putProxyClass(target.getName(), proxyClass);
                }
            }
        }

        Object proxyObject;

        try {
            Enhancer.registerCallbacks(proxyClass, new Callback[] {new AsyncResultInterceptor(future),
                new AsyncProxyResultInterceptor(future),
                new AsyncProxySerializeInterceptor(),
                new AsyncToStringMethodInterceptor(future)});
            proxyObject = AsyncProxyUtils.newInstance(proxyClass);
        } finally {
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
        return proxyObject;
    }

    static class AsyncResultCallbackFilter implements CallbackFilter {

        @Override
        public int accept(Method method) {
            if (AsyncProxyResultSupport.class.isAssignableFrom(method.getDeclaringClass())) {
                return 1;
            }
            if ("writeReplace".equals(method.getName())) {
                return 2;
            }
            if (ReflectionUtils.isToStringMethod(method)) {
                return 3;
            }
            return 0;
        }
    }

    static class AsyncResultInterceptor implements LazyLoader {

        private AsyncFutureTask<?> future;

        public AsyncResultInterceptor(AsyncFutureTask<?> future) {
            this.future = future;
        }

        @Override
        public Object loadObject() throws Exception {
            return future.getValue();
        }
    }

    static class AsyncToStringMethodInterceptor implements MethodInterceptor {

        private AsyncFutureTask<?> future;

        public AsyncToStringMethodInterceptor(AsyncFutureTask<?> future) {
            this.future = future;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Object value = future.getValue();
            if (value != null) {
                return value.toString();
            }
            return null;
        }
    }

    static class AsyncProxyResultInterceptor implements MethodInterceptor {

        private AsyncFutureTask<?> future;

        public AsyncProxyResultInterceptor(AsyncFutureTask<?> future) {
            this.future = future;
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {

            if ("_isNull".equals(method.getName())) {
                return future.getValue() == null;
            }
            if ("_getResult".equals(method.getName())) {
                return future.getValue();
            }
            if ("_getFuture".equals(method.getName())) {
                return future;
            }

            throw new AsyncException("method[" + method.getName() + "] is not support!");
        }

        public AsyncFutureTask getFuture() {
            return future;
        }
    }

    static class AsyncProxySerializeInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
            return AsyncProxyUtils.getTargetObject(o);
        }
    }
}
