package cn.ideabuffer.retry;

import cn.ideabuffer.retry.script.AbstractScriptParser;
import cn.ideabuffer.retry.script.SpringELParser;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author sangjian.sj
 * @date 2019/05/27
 */
public final class ResultPredicats {

    public static final Double DOUBLE_ZERO = 0.0d;

    public static final Float FLOAT_ZERO = 0.0f;

    public static final Predicate<String> IS_EMPTY_STRING_PREDICATE = new IsEmptyString();

    public static final IsEmptyCollection IS_EMPTY_COLLECTION_PREDICATE = new IsEmptyCollection();

    public static final IsEmptyMap IS_EMPTY_MAP_PREDICATE = new IsEmptyMap<>();

    private ResultPredicats() {}

    public static <T> Predicate<T> isNull() {
        return ObjectPredicate.IS_NULL.withNarrowedType();
    }

    public static <T> Predicate<T> isNotNull() {
        return Predicates.not(isNull());
    }

    public static Predicate<Boolean> isTrue() {
        return BooleanPredicat.IS_TRUE;
    }

    public static Predicate<Boolean> isFalse() {
        return BooleanPredicat.IS_FALSE;
    }

    public static Predicate<Number> isZero() {
        return NumberPredicate.IS_ZEOR;
    }

    public static Predicate<Number> isNegative() {
        return NumberPredicate.IS_NEGATIVE;
    }

    public static Predicate<Number> isPositive() {
        return NumberPredicate.IS_POSITIVE;
    }

    public static Predicate<String> isEmptyString() {
        return IS_EMPTY_STRING_PREDICATE;
    }

    public static Predicate<String> isNotEmptyString() {
        return Predicates.not(IS_EMPTY_STRING_PREDICATE);
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<Collection<? extends T>> isEmptyCollection() {
        return (Predicate<Collection<? extends T>>)IS_EMPTY_COLLECTION_PREDICATE;
    }


    public static <T> Predicate<Collection<? extends T>> isNotEmptyCollection() {
        return Predicates.not(isEmptyCollection());
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Predicate<Map<K, V>> isEmptyMap() {
        return (Predicate<Map<K, V>>)IS_EMPTY_MAP_PREDICATE;
    }

    public static <K, V> Predicate<Map<K, V>> isNotEmptyMap() {
        return Predicates.not(isEmptyMap());
    }

    public static Predicate<Boolean> isExprTrue(String expr, AbstractScriptParser parser) {
        return new ExpressionPredicate<>(expr, parser);
    }

    enum ObjectPredicate implements Predicate<Object> {

        /**
         * 值为null
         */
        IS_NULL {
            @Override
            public boolean apply(@Nullable Object o) {
                return o == null;
            }

            @Override
            public String toString() {
                return "Predicates.isNull()";
            }
        };

        @SuppressWarnings("unchecked") // safe contravariant cast
        <T> Predicate<T> withNarrowedType() {
            return (Predicate<T>) this;
        }
    }

    enum BooleanPredicat implements Predicate<Boolean> {
        /**
         * 是否为true
         */
        IS_TRUE {

            @Override
            public boolean apply(@Nullable Boolean input) {
                return Boolean.TRUE.equals(input);
            }


            @Override
            public String toString() {
                return "ResultPredicats.isTrue()";
            }
        },
        /**
         * 是否为false
         */
        IS_FALSE {

            @Override
            public boolean apply(@Nullable Boolean input) {
                return Boolean.FALSE.equals(input);
            }


            @Override
            public String toString() {
                return "ResultPredicats.isFalse()";
            }
        }
    }

    enum NumberPredicate implements Predicate<Number> {
        /**
         * 值为0
         */
        IS_ZEOR {
            @Override
            public boolean apply(Number input) {
                checkNotNull(input);
                if(input instanceof Float) {
                    return Float.compare((Float)input, FLOAT_ZERO) == 0;
                }
                if(input instanceof Double) {
                    return Double.compare((Double)input, DOUBLE_ZERO) == 0;
                }
                return input.longValue() == 0;
            }


            @Override
            public String toString() {
                return "ResultPredicats.isZero()";
            }
        },
        /**
         * 值为负数
         */
        IS_NEGATIVE {
            @Override
            public boolean apply(Number input) {
                checkNotNull(input);
                if(input instanceof Float) {
                    return Float.compare((Float)input, FLOAT_ZERO) < 0;
                }
                if(input instanceof Double) {
                    return Double.compare((Double)input, DOUBLE_ZERO) < 0;
                }
                return input.longValue() < 0;
            }


            @Override
            public String toString() {
                return "ResultPredicats.isNegative()";
            }
        },
        /**
         * 值为正数
         */
        IS_POSITIVE {
            @Override
            public boolean apply(Number input) {
                checkNotNull(input);
                if(input instanceof Float) {
                    return Float.compare((Float)input, FLOAT_ZERO) > 0;
                }
                if(input instanceof Double) {
                    return Double.compare((Double)input, DOUBLE_ZERO) > 0;
                }
                return input.longValue() > 0;
            }


            @Override
            public String toString() {
                return "ResultPredicats.isPositive()";
            }
        }
    }

    private static class IsEmptyString implements Predicate<String>, Serializable {

        @Override
        public boolean apply(String value) {
            return value == null || "".equals(value);
        }

        @Override
        public String toString() {
            return "Predicates.IsEmptyString()";
        }

    }

    private static class IsEmptyCollection<T> implements Predicate<Collection<? extends T>>, Serializable {

        @Override
        public boolean apply(@Nullable Collection<? extends T> input) {
            return input == null || input.size() == 0;
        }

        @Override
        public String toString() {
            return "Predicates.IsEmptyCollection()";
        }

    }

    private static class IsEmptyMap<K, V> implements Predicate<Map<K, V>>, Serializable {

        @Override
        public boolean apply(@Nullable Map<K, V> input) {
            return input == null || input.size() == 0;
        }

        @Override
        public String toString() {
            return "Predicates.IsEmptyCollection()";
        }

    }

    private static class ExpressionPredicate<T> implements Predicate<T>, Serializable {

        private String expression;

        private AbstractScriptParser parser;

        public ExpressionPredicate(String expression, AbstractScriptParser parser) {
            this.expression = expression;
            this.parser = parser;
        }

        @Override
        public boolean apply(@Nullable T input) {
            Map<String, Object> varMap = new HashMap<>();
            varMap.put("retVal", input);
            try {
                Boolean value = parser.getElValue(expression, varMap, Boolean.class);
                return Boolean.TRUE.equals(value);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("");
            }
        }
    }


}
