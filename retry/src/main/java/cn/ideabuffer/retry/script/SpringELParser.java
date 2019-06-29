package cn.ideabuffer.retry.script;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sangjian.sj
 * @date 2019/02/17
 */
public class SpringELParser extends AbstractScriptParser {

    /**
     * # 号
     */
    private static final String POUND = "#";

    /**
     * 撇号
     */
    private static final String apostrophe = "'";

    private final ExpressionParser parser = new SpelExpressionParser();

    private final ConcurrentHashMap<String, Expression> expCache = new ConcurrentHashMap<>();

    private static Method hash = null;

    private static Method empty = null;


    private final ConcurrentHashMap<String, Method> funcs = new ConcurrentHashMap<>(8);

    /**
     * @param name   方法名
     * @param method 方法
     */
    @Override
    public void addFunction(String name, Method method) {
        funcs.put(name, method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getElValue(String expr, Object[] arguments, Object retVal, boolean hasRetVal,
        Class<T> valueType) throws Exception {
        if (valueType.equals(String.class)) {
            // 如果不是表达式，直接返回字符串
            if (!expr.contains(POUND) && !expr.contains(apostrophe)) {
                return (T) expr;
            }
        }
        StandardEvaluationContext context = new StandardEvaluationContext();

        Iterator<Map.Entry<String, Method>> it = funcs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Method> entry = it.next();
            context.registerFunction(entry.getKey(), entry.getValue());
        }
        context.setVariable(ARGS, arguments);
        if (hasRetVal) {
            context.setVariable(RET_VAL, retVal);
        }
        Expression expression = expCache.get(expr);
        if (null == expression) {
            expression = parser.parseExpression(expr);
            expCache.put(expr, expression);
        }
        return expression.getValue(context, valueType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getElValue(String expr, Map<String, Object> varMap, Class<T> valueType) throws Exception {
        if (valueType.equals(String.class)) {
            // 如果不是表达式，直接返回字符串
            if (!expr.contains(POUND) && !expr.contains(apostrophe)) {
                return (T) expr;
            }
        }
        StandardEvaluationContext context = new StandardEvaluationContext();

        if(varMap != null) {
            for (Map.Entry<String, Object> entry : varMap.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }
        }

        Expression expression = expCache.get(expr);
        if (null == expression) {
            expression = parser.parseExpression(expr);
            expCache.put(expr, expression);
        }
        return expression.getValue(context, valueType);
    }

    public static void main(String[] args) {
        String exp = "{test:#argMap.get('name')}";

        Map<String, Object> map = new HashMap<>();
        map.put("name", "sangjian");
        map.put("age", 29);

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("argMap", map);
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(exp);
        Map<String, Object> parsedMap = expression.getValue(context, Map.class);
        System.out.println(parsedMap);
    }

}
