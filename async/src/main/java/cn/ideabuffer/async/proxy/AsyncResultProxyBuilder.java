package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.cache.AsyncProxyCache;
import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.core.AsyncProxyResultSupport;
import cn.ideabuffer.async.exception.AsyncException;
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

    public static Object buildProxy(Object target, AsyncFutureTask<?> future) {
        if (!(target instanceof Class)) {
            logger.error("target:{} type is not Class", target);
            throw new IllegalArgumentException(
                String.format("targetType:%s is not Class", target.getClass().getName()));
        }
        Class<?> returnClass = (Class)target;

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
        if (proxyClass == null) {
            synchronized (AsyncResultProxyBuilder.class) {
                proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
                if (proxyClass == null) {
                    Enhancer enhancer = new Enhancer();
                    if (returnClass.isInterface()) {
                        enhancer.setInterfaces(
                            new Class[] {AsyncProxyResultSupport.class, returnClass, AsyncSerializable.class});
                    } else {
                        enhancer.setInterfaces(new Class[] {AsyncProxyResultSupport.class, AsyncSerializable.class});
                        enhancer.setSuperclass(returnClass);
                    }

                    enhancer.setCallbackFilter(new AsyncResultCallbackFilter(future));
                    enhancer.setCallbackTypes(
                        new Class[] {AsyncResultInterceptor.class, AsyncProxyResultInterceptor.class,
                            AsyncProxySerializeInterceptor.class, AsyncToStringMethodInterceptor.class});
                    proxyClass = enhancer.createClass();
                    logger.debug("create result proxy class:{}, proxyClass:{}", returnClass, proxyClass);
                    AsyncProxyCache.putProxyClass(returnClass.getName(), proxyClass);
                }
            }
        }

        Object proxyObject = null;

        try {
            Enhancer.registerCallbacks(proxyClass, new Callback[] {new AsyncResultInterceptor(future),
                new AsyncProxyResultInterceptor(future),
                new AsyncProxySerializeInterceptor(),
                new AsyncToStringMethodInterceptor(future)});
            proxyObject = AsyncProxyUtils.newInstance(proxyClass);
        } catch (Exception e) {

        } finally {
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
        return proxyObject;
    }

    static class AsyncResultCallbackFilter implements CallbackFilter {

        private AsyncFutureTask<?> future;

        public AsyncResultCallbackFilter(AsyncFutureTask<?> future) {
            this.future = future;
        }

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
                logger.info("in toString interceptor, valueClass:{}, thread:{}", value.getClass().getName(),
                    Thread.currentThread().getName());
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
            return AsyncProxyUtils.getCglibProxyTargetObject(o);
        }
    }
}
