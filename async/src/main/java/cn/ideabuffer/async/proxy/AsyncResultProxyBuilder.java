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
            throw new IllegalArgumentException(String.format("targetType:%s is not Class", target.getClass().getName()));
        }
        Class<?> returnClass = (Class)target;

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (returnClass.isInterface()) {
                enhancer.setInterfaces(
                    new Class[] {AsyncProxyResultSupport.class, returnClass, CglibSerializable.class});
            } else {
                enhancer.setInterfaces(new Class[] {AsyncProxyResultSupport.class, CglibSerializable.class});
                enhancer.setSuperclass(returnClass);
            }
            enhancer.setCallbackFilter(new AsyncResultCallbackFilter());
            enhancer.setCallbackTypes(new Class[] {AsyncResultInterceptor.class, AsyncProxyResultInterceptor.class,
                AsyncProxySerializeInterceptor.class, AsyncObjectMethodInterceptor.class});
            proxyClass = enhancer.createClass();
            logger.debug("create result proxy class:{}", returnClass);
            AsyncProxyCache.putProxyClass(returnClass.getName(), proxyClass);
        }
        Enhancer.registerCallbacks(proxyClass, new Callback[] {new AsyncResultInterceptor(future),
            new AsyncProxyResultInterceptor(),
            new AsyncProxySerializeInterceptor(),
            new AsyncObjectMethodInterceptor()});
        Object proxyObject;
        try {
            proxyObject = AsyncProxyUtils.newInstance(proxyClass);
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
            if (ReflectionUtils.isToStringMethod(method)
                || ReflectionUtils.isEqualsMethod(method)
                || ReflectionUtils.isHashCodeMethod(method)) {
                return 3;
            }
            return 0;
        }
    }

    class AsyncObjectMethodInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Object value = future.getValue();
            if(value != null) {
                return value;
            }
            if (ReflectionUtils.isEqualsMethod(method)) {
                return false;
            }
            if(ReflectionUtils.isHashCodeMethod(method)) {
                return -1;
            }
            if(ReflectionUtils.isToStringMethod(method)) {
                return "null";
            }
            return null;
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

            throw new AsyncException("method[" + method.getName() + "] is not support!");
        }
    }

    class AsyncProxySerializeInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) {
            return AsyncProxyUtils.getCglibProxyTargetObject(o);
        }
    }
}
