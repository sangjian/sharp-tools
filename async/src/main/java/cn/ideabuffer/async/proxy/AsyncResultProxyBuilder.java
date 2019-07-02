package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.core.AsyncProxyResult;
import cn.ideabuffer.async.exception.AsyncException;
import net.sf.cglib.proxy.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if(!(target instanceof Class)) {
            throw new RuntimeException();
        }
        Class<?> returnClass =  (Class) target;

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(returnClass.getName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (returnClass.isInterface()) {
                enhancer.setInterfaces(new Class[]{AsyncProxyResult.class, returnClass, CglibSerializable.class});
            } else {
                enhancer.setInterfaces(new Class[]{AsyncProxyResult.class, CglibSerializable.class});
                enhancer.setSuperclass(returnClass);
            }
            enhancer.setCallbackFilter(new AsyncResultCallbackFilter());
            enhancer.setCallbackTypes(new Class[] {AsyncProxyResultInterceptor.class, AsyncResultInterceptor.class, AsyncProxySerializeInterceptor.class});
            proxyClass = enhancer.createClass();
            logger.debug("create result proxy class:{}", returnClass);
            AsyncProxyCache.putProxyClass(AsyncProxyUtils.getOriginClass(target).getName(), proxyClass);
        }
        Enhancer.registerCallbacks(proxyClass, new Callback[]{new AsyncProxyResultInterceptor(),
            new AsyncResultInterceptor(future),
        new AsyncProxySerializeInterceptor()});
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
            if(AsyncProxyResult.class.isAssignableFrom(method.getDeclaringClass())) {
                return 0;
            }
            if("writeReplace".equals(method.getName())) {
                return 2;
            } else {
                return 1;
            }

        }
    }

    class AsyncProxyResultInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
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
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            Object obj = AsyncProxyUtils.getCglibProxyTargetObject(o);
            return obj;

        }
    }
}
