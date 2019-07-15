package cn.ideabuffer.async.spring;

import cn.ideabuffer.async.annotation.Async;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author sangjian.sj
 * @date 2019/07/13
 */
public class AsyncAutoProxyCreator extends AbstractAutoProxyCreator {

    private AsyncAutoProxyMethodInterceptor asyncAutoProxyMethodInterceptor;

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) {
        Method[] methods = beanClass.getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            return DO_NOT_PROXY;
        }
        for (Method method : methods) {
            Async annotation = AnnotationUtils.findAnnotation(method, Async.class);
            if (annotation != null && Modifier.isPublic(method.getModifiers())) {
                return new Object[]{asyncAutoProxyMethodInterceptor};
            }
        }
        return DO_NOT_PROXY;
    }

    public void setAsyncAutoProxyMethodInterceptor(
        AsyncAutoProxyMethodInterceptor asyncAutoProxyMethodInterceptor) {
        this.asyncAutoProxyMethodInterceptor = asyncAutoProxyMethodInterceptor;
    }
}
