package cn.ideabuffer.async.proxy;

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
public class AsyncResultProxyBuilder implements AsyncProxyBuilder {

    private final static Logger logger = LoggerFactory.getLogger(AsyncResultProxyBuilder.class);

    private AsyncFutureTask future;

    public AsyncResultProxyBuilder(AsyncFutureTask future) {
        this.future = future;
    }

    @Override
    public Object buildProxy(Object target) {
        if (!(target instanceof Class)) {
            logger.error("target:{} type is not Class", target);
            throw new IllegalArgumentException(
                String.format("targetType:%s is not Class", target.getClass().getName()));
        }
        Class<?> returnClass = (Class)target;

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
        if (proxyClass == null) {
            synchronized (this) {
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

                    enhancer.setCallbackFilter(new AsyncResultCallbackFilter());
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
            Enhancer.registerCallbacks(proxyClass, new Callback[] {new AsyncResultInterceptor(),
                new AsyncProxyResultInterceptor(),
                new AsyncProxySerializeInterceptor(),
                new AsyncToStringMethodInterceptor()});
            proxyObject = AsyncProxyUtils.newInstance(proxyClass);

        } catch (Exception e) {

        } finally {
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
        return proxyObject;
    }

    class AsyncResultCallbackFilter implements CallbackFilter {

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

    class AsyncResultInterceptor implements LazyLoader {

        @Override
        public Object loadObject() throws Exception {
            return future.getValue();
        }
    }

    class AsyncToStringMethodInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Object value = future.getValue();
            if (value != null) {
                logger.info("in toString interceptor, valueClass:{}, thread:{}", value.getClass().getName(),
                    Thread.currentThread().getName());
                return value.toString();
            }
            return "null";
        }
    }

    class AsyncProxyResultInterceptor implements MethodInterceptor {

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

    class AsyncProxySerializeInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
            return AsyncProxyUtils.getCglibProxyTargetObject(o);
        }
    }
}
