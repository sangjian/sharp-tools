package cn.ideabuffer.async.spring;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sangjian.sj
 * @date 2019/07/15
 */
public class AsyncBeanInitAutoProxyCreator extends BeanNameAutoProxyCreator implements ApplicationContextAware {

    private List<String> parallelInitBeanNames;

    private boolean parallelAll;

    private ApplicationContext applicationContext;

    private ExecutorService beanInitExecutor = Executors.newFixedThreadPool(4);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setParallelInitBeanNames(String... parallelInitBeanNames) {
        Assert.notEmpty(parallelInitBeanNames, "'parallelInitBeanNames' must not be empty");
        super.setBeanNames(parallelInitBeanNames);
        this.parallelInitBeanNames = new ArrayList<>(parallelInitBeanNames.length);
        for (String mappedName : parallelInitBeanNames) {
            this.parallelInitBeanNames.add(StringUtils.trimWhitespace(mappedName));
        }
    }

    public void setParallelAll(boolean parallelAll) {
        this.parallelAll = parallelAll;
    }

    public void setBeanInitExecutor(ExecutorService beanInitExecutor) {
        this.beanInitExecutor = beanInitExecutor;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
        if(parallelAll) {
            return doGetAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
        }
        if (this.parallelInitBeanNames != null) {
            for (String mappedName : this.parallelInitBeanNames) {
                if (FactoryBean.class.isAssignableFrom(beanClass)) {
                    if (!mappedName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
                        continue;
                    }
                    mappedName = mappedName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
                }
                if (isMatch(beanName, mappedName)) {
                    return doGetAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
                }
                BeanFactory beanFactory = getBeanFactory();
                if (beanFactory != null) {
                    String[] aliases = beanFactory.getAliases(beanName);
                    for (String alias : aliases) {
                        if (isMatch(alias, mappedName)) {
                            return doGetAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
                        }
                    }
                }
            }
        }
        return DO_NOT_PROXY;
    }

    protected Object[] doGetAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName,
        TargetSource customTargetSource) throws BeansException {
        Method[] methods = beanClass.getDeclaredMethods();
        if (methods == null || methods.length == 0) {
            return DO_NOT_PROXY;
        }
        Class<?>[] interfaces = beanClass.getInterfaces();
        boolean isInitializingBean = false;
        if(interfaces != null && interfaces.length > 0) {
            for (Class<?> clazz : interfaces) {
                if(clazz == InitializingBean.class) {
                    isInitializingBean = true;
                    break;
                }
            }
        }
        boolean hasInitMethod = false;
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        BeanDefinition beanDefinition = null;
        if(beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            try {
                beanDefinition = ((AbstractAutowireCapableBeanFactory)beanFactory).getMergedBeanDefinition(beanName);
            } catch (Exception e) {
                return DO_NOT_PROXY;
            }

        }
        String initMethodName = null;
        if(beanDefinition instanceof RootBeanDefinition) {
            initMethodName = ((RootBeanDefinition)beanDefinition).getInitMethodName();
            RootBeanDefinition rbd = (RootBeanDefinition)beanDefinition;
            if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
                !rbd.isExternallyManagedInitMethod(initMethodName)) {
                hasInitMethod = true;
            }
        }

        if(isInitializingBean || hasInitMethod) {
            return new Object[]{new AsyncBeanInitAutoProxyMethodInterceptor(initMethodName, beanInitExecutor)};
        }
        return DO_NOT_PROXY;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
