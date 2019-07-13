package cn.ideabuffer.async.processor;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.proxy.AsyncProxyUtils;
import cn.ideabuffer.async.proxy.ProxyTypeEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncMethodBeanProcessor implements BeanPostProcessor {



    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return processAasyncBean(bean, beanName);
    }

    public Object processAasyncBean(Object bean, String beanName) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            return bean;
        }
        for (Method method : methods) {
            Async annotation = AnnotationUtils.findAnnotation(method, Async.class);
            if (annotation != null && Modifier.isPublic(method.getModifiers())) {
                return AsyncProxyUtils.getAsyncMethodProxyBuilder(ProxyTypeEnum.CGLIB).buildProxy(bean);
            }
        }
        return bean;

    }
}
