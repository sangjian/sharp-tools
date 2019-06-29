package cn.ideabuffer.retry.script;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author sangjian.sj
 * @date 2019/02/17
 */
public abstract class AbstractScriptParser {

    protected static final String TARGET = "target";

    protected static final String ARGS = "args";

    protected static final String RET_VAL = "retVal";

    protected static final String HASH = "hash";

    protected static final String EMPTY = "empty";

    /**
     * 为了简化表达式，方便调用Java static 函数，在这里注入表达式自定义函数
     *
     * @param name   自定义函数名
     * @param method 调用的方法
     */
    public abstract void addFunction(String name, Method method);

    /**
     * 将表达式转换期望的值
     *
     * @param expr       表达式
     * @param arguments 参数
     * @param valueType 表达式最终返回值类型
     * @param <T>       泛型
     * @return T value 返回值
     * @throws Exception 异常
     */
    public abstract <T> T getElValue(String expr, Object[] arguments, Object retVal, boolean hasRetVal, Class<T> valueType) throws

        Exception;


    public abstract <T> T getElValue(String expr, Map<String, Object> varMap, Class<T> valueType) throws Exception;
}
