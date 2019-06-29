package cn.ideabuffer.async.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncMethodProxyBuilder implements AsyncProxyBuilder {

    private final static Logger logger = LoggerFactory.getLogger(AsyncMethodProxyBuilder.class);

    @Override
    public Object buildProxy(Object target) {
        Class<?> targetClass = AsyncProxyUtils.getOriginClass(target);
        if (target instanceof Class) {
            throw new RuntimeException("target is not object instance");
        }

        if (!AsyncProxyUtils.canProxy(targetClass)) {
            return target;
        }

        Class<?> proxyClass = AsyncProxyCache.getProxyClass(AsyncProxyUtils.getOriginClass(target).getName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (targetClass.isInterface()) {
                enhancer.setInterfaces(new Class[]{targetClass});
            } else {
                enhancer.setSuperclass(targetClass);
            }
            enhancer.setCallbackType(AsyncMethodInterceptor.class);
            proxyClass = enhancer.createClass();
            logger.debug("create proxy class:{}", targetClass);
            AsyncProxyCache.putProxyClass(AsyncProxyUtils.getOriginClass(target).getName(), proxyClass);
            AsyncProxyCache.putAllProxyMethod(target);
        }
        Enhancer.registerCallbacks(proxyClass, new Callback[]{new AsyncMethodInterceptor(target)});
        Object proxyObject;
        try {
            proxyObject = AsyncProxyUtils.newInstance(proxyClass);
        } finally {
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }

        return proxyObject;
    }
}
