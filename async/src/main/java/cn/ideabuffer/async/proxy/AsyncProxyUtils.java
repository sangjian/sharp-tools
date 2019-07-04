package cn.ideabuffer.async.proxy;

import cn.ideabuffer.async.core.AsyncFutureTask;
import cn.ideabuffer.async.core.AsyncProxyResultSupport;
import cn.ideabuffer.async.exception.AsyncException;
import net.sf.cglib.core.ReflectUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sangjian.sj
 * @date 2019/06/18
 */
public class AsyncProxyUtils {

    private static AsyncProxyBuilder cgLibMethodProxyBuilder = new AsyncMethodProxyBuilder();

    private static final Map<Class<?>, Object> PRIMITIVE_VALUE_MAP = new HashMap<>(16);

    static {
        PRIMITIVE_VALUE_MAP.put(Boolean.class, Boolean.FALSE);
        PRIMITIVE_VALUE_MAP.put(Byte.class, Byte.valueOf((byte) 0));
        PRIMITIVE_VALUE_MAP.put(Character.class, Character.valueOf((char) 0));
        PRIMITIVE_VALUE_MAP.put(Short.class, Short.valueOf((short) 0));
        PRIMITIVE_VALUE_MAP.put(Double.class, Double.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(Float.class, Float.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(Integer.class, Integer.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(Long.class, Long.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(boolean.class, Boolean.FALSE);
        PRIMITIVE_VALUE_MAP.put(byte.class, Byte.valueOf((byte) 0));
        PRIMITIVE_VALUE_MAP.put(char.class, Character.valueOf((char) 0));
        PRIMITIVE_VALUE_MAP.put(short.class, Short.valueOf((short) 0));
        PRIMITIVE_VALUE_MAP.put(double.class, Double.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(float.class, Float.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(int.class, Integer.valueOf(0));
        PRIMITIVE_VALUE_MAP.put(long.class, Long.valueOf(0));
    }

    public static Object newInstance(Class type) {
        Constructor constructor = null;
        Object[] constructorArgs = new Object[0];
        try {
            constructor = type.getConstructor(new Class[]{});// 先尝试默认的空构造函数
        } catch (NoSuchMethodException e) {
            // ignore
        }

        // 没有默认的构造函数，尝试别的带参数的函数
        if (constructor == null) {
            Constructor[] constructors = type.getConstructors();
            if (constructors == null || constructors.length == 0) {
                throw new UnsupportedOperationException("Class[" + type.getName() + "] has no public constructors");
            }
            // 默认取第一个参数
            constructor = constructors[getSimpleParamenterTypeIndex(constructors)];
            Class[] params = constructor.getParameterTypes();
            constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                constructorArgs[i] = getDefaultValue(params[i]);
            }
        }

        return ReflectUtils.newInstance(constructor, constructorArgs);
    }

    public static int getSimpleParamenterTypeIndex(Constructor[] constructors) {
        Constructor constructor;
        Class[] params;
        boolean isSimpleTypes;
        for (int i = 0; i < constructors.length; i++) {
            constructor = constructors[i];
            params = constructor.getParameterTypes();
            if (params.length > 0) {
                isSimpleTypes = true;
                for (int j = 0; j < params.length; j++) {
                    if (PRIMITIVE_VALUE_MAP.get(params[j]) == null) {
                        isSimpleTypes = false;
                        break;
                    }
                }
                if (isSimpleTypes) {
                    return i;
                }
            } else {
                return i;
            }
        }
        return 0;
    }

    public static Object getDefaultValue(Class cl) {
        if (cl.isArray()) {
            return Array.newInstance(cl.getComponentType(), 0);
        } else if (cl.isPrimitive() || PRIMITIVE_VALUE_MAP.containsKey(cl)) {
            return PRIMITIVE_VALUE_MAP.get(cl);
        } else {
            return null;
        }
    }

    public static AsyncProxyBuilder getAsyncMethodProxyBuilder(ProxyTypeEnum proxyType) {
        switch (proxyType) {
            case CGLIB:
                return cgLibMethodProxyBuilder;
            default:
                break;
        }
        return cgLibMethodProxyBuilder;
    }

    public static Class<?> getOriginClass(Object object) {
        boolean isCglibProxy = false;
        if (AopUtils.isCglibProxy(object)) {
            isCglibProxy = true;
        }
        if (!isCglibProxy) {
            isCglibProxy = ClassUtils.isCglibProxy(object);
        }
        Class<?> targetClass = object.getClass();
        if (isCglibProxy) {
            targetClass = targetClass.getSuperclass();
        }
        return targetClass;
    }

    public static boolean isVoid(Class cls) {
        if (Void.class.equals(cls)
            || Void.TYPE.isAssignableFrom(cls)) {
            return true;
        }
        return false;
    }

    public static boolean canProxy(Class cls) {
        if (Void.class.equals(cls)
            || Void.TYPE.isAssignableFrom(cls)
            || !Modifier.isPublic(cls.getModifiers())
            || Modifier.isFinal(cls.getModifiers())
            || cls.isArray()
            || cls.isPrimitive()
            || cls == Object.class) {
            return false;
        }
        return true;
    }

    public static String genMethodKey(Object target, Method method) {

        StringBuilder key = new StringBuilder();

        key.append(getOriginClass(target).getName())
            .append("#")
            .append(method.getName());
        Class<?>[] paramTypes = method.getParameterTypes();
        if(paramTypes == null || paramTypes.length == 0) {
            return key.toString();
        }
        for (Class<?> type : paramTypes) {
            key.append("@")
                .append(type.getName());
        }
        return key.toString();
    }

    public static boolean isNull(Object returnObj) throws AsyncException {
        if (!(returnObj instanceof AsyncProxyResultSupport)) {
            return returnObj == null;
        } else {
            return ((AsyncProxyResultSupport) returnObj)._isNull();
        }
    }

    public static Object getCglibProxyTargetObject(Object proxy) {
        try{
            Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
            h.setAccessible(true);
            Object callbackObject = h.get(proxy);
            Field this0 = callbackObject.getClass().getDeclaredField("this$0");
            this0.setAccessible(true);
            Object futureObject = this0.get(callbackObject);
            Field future = futureObject.getClass().getDeclaredField("future");
            future.setAccessible(true);
            return  ((AsyncFutureTask<?>)future.get(futureObject)).getValue();
        } catch(Exception e){
            throw new AsyncException("getCglibProxyTargetObject error", e);
        }
    }
}
