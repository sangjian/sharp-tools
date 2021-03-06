package cn.ideabuffer.async.cache;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.bean.AsyncMethod;
import cn.ideabuffer.async.util.AsyncProxyUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static cn.ideabuffer.async.constant.AsyncConstants.DEFAULT_EXECUTOR_NAME;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncProxyCache {

    /**
     * 代理class缓存
     */
    private static ConcurrentMap<String, Class<?>> PROXY_CLASS_CACHE = new ConcurrentHashMap<>(128);

    /**
     * 代理方法缓存
     */
    private static ConcurrentMap<String, AsyncMethod> PROXY_METHOD_CACHE = new ConcurrentHashMap<>(128);

    public static void putProxyClass(String key, Class<?> proxyClass) {
        PROXY_CLASS_CACHE.putIfAbsent(key, proxyClass);
    }

    public static void putProxyMethod(String key, AsyncMethod asyncMethod) {
        PROXY_METHOD_CACHE.putIfAbsent(key, asyncMethod);
    }

    /**
     * 注册所有Async方法
     * @param target
     */
    public static void putAllProxyMethod(Object target) {
        // 获取原始对象
        Object proxiedObj = AsyncProxyUtils.getTargetObject(target);
        if(proxiedObj == null) {
            return;
        }
        Method[] methods = proxiedObj.getClass().getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            return;
        }


        for (Method method : methods) {
            Async annotation = AnnotationUtils.findAnnotation(method, Async.class);
            if (annotation != null && Modifier.isPublic(method.getModifiers())) {
                String executorName = annotation.value();
                if ("".equals(executorName)) {
                    executorName = DEFAULT_EXECUTOR_NAME;
                }
                AsyncMethod asyncMethod = new AsyncMethod(target, method, annotation.timeout(), executorName, null,
                    annotation.allowThreadLocalTransfer(), annotation.allowCascade());
                putProxyMethod(AsyncProxyUtils.genMethodKey(method), asyncMethod);
            }
        }
    }

    public static Class<?> getProxyClass(String key) {
        return PROXY_CLASS_CACHE.get(key);
    }

    public static AsyncMethod getProxyMethod(String key) {
        return PROXY_METHOD_CACHE.get(key);
    }

}
