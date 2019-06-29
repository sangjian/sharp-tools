package cn.ideabuffer.retry.interceptor;

import cn.ideabuffer.retry.ResultPredicats;
import cn.ideabuffer.retry.annotation.BackOff;
import cn.ideabuffer.retry.annotation.Retry;
import cn.ideabuffer.retry.script.SpringELParser;
import com.github.rholder.retry.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author sangjian.sj
 * @date 2019/05/23
 */
public class RetryInterceptor {

    private Map<Method, Retryer> retryerMap = new ConcurrentHashMap<>();

    private SpringELParser parser = new SpringELParser();

    public Object interceptAround(ProceedingJoinPoint pjp, Retry retry) throws Throwable {
        Signature signature = pjp.getSignature();
        Object[] args = pjp.getArgs();

        Method method = ((MethodSignature)signature).getMethod();

        Retryer retryer = retryerMap.get(method);

        if(retryer == null) {
            retryer = buildRetryer(retry);
            retryerMap.putIfAbsent(method, retryer);
        }
        Object result;
        try {
            result = retryer.call(() -> {
                try {
                    return pjp.proceed(args);
                } catch (Exception e) {
                    throw e;
                } catch (Throwable throwable) {
                    throw new Exception(throwable);
                }
            });
        } catch (RetryException retryExeption) {
            return retryExeption.getLastFailedAttempt().getResult();
        } catch (Exception e) {
            throw e;
        }


        return result;
    }

    @SuppressWarnings("unchecked")
    private Retryer buildRetryer(Retry retry) {

        int maxAttempts = retry.maxAttempts();
        int timeout = retry.timeout();
        Class<? extends Exception>[] exceptionTypes = retry.retryIfExceptionOfType();
        boolean retryIfRumTimeException = retry.retryIfRuntimeException();
        String retryIfExpr = retry.retryIfExpr();
        Retry.StopStrategy stopStrategy = retry.stopStrategy();
        BackOff backOff = retry.backOff();

        RetryerBuilder retryerBuilder = RetryerBuilder.newBuilder();
        if(exceptionTypes.length > 0) {
            for (Class<? extends Exception> cls : exceptionTypes) {
                retryerBuilder.retryIfExceptionOfType(cls);
            }
        }
        if(retryIfRumTimeException) {
            retryerBuilder.retryIfRuntimeException();
        }

        switch (stopStrategy) {
            case NEVER_STOP:
                retryerBuilder.withStopStrategy(StopStrategies.neverStop());
                break;
            case STOP_AFTER_ATTEMT:
                retryerBuilder.withStopStrategy(StopStrategies.stopAfterAttempt(maxAttempts));
                break;
            case STOP_AFTER_TIMEOUT:
                retryerBuilder.withStopStrategy(StopStrategies.stopAfterDelay(timeout, TimeUnit.MILLISECONDS));
                break;
            default:
                break;
        }

        int fixSleepTime = backOff.fixSleepTime();
        int multiplier = backOff.multiplier();
        int increment = backOff.increment();
        int increInitTime = backOff.increInitTime();
        int randomMinTime = backOff.randomMinTime();
        int randomMaxTime = backOff.randomMaxTime();
        int fibMaxTime = backOff.fibMaxTime();
        int expMaxTime = backOff.expMaxTime();
        BackOff.WaitStrategy waitStrategy = backOff.waitStrategy();

        switch (waitStrategy) {
            case NO_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.noWait());
                break;
            case FIX_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.fixedWait(fixSleepTime, TimeUnit.MILLISECONDS));
                break;
            case RANDOM_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.randomWait(randomMinTime, TimeUnit.MILLISECONDS, randomMaxTime, TimeUnit.MILLISECONDS));
                break;
            case INCREMENT_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.incrementingWait(increInitTime, TimeUnit.MILLISECONDS, increment, TimeUnit.MILLISECONDS));
                break;
            case FIBONACCI_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.fibonacciWait(multiplier, fibMaxTime, TimeUnit.MILLISECONDS));
                break;
            case EXPONENTIAL_WAIT:
                retryerBuilder.withWaitStrategy(WaitStrategies.exponentialWait(multiplier, expMaxTime, TimeUnit.MILLISECONDS));
                break;
            default:
                break;
        }

        Retry.RetryResult[] retryResults = retry.retryIfResult();

        for (Retry.RetryResult result : retryResults) {
            switch (result) {
                case IS_NULL:
                    retryerBuilder.retryIfResult(ResultPredicats.isNull());
                    break;
                case IS_NOT_NULL:
                    retryerBuilder.retryIfResult(ResultPredicats.isNotNull());
                    break;
                case IS_TRUE:
                    retryerBuilder.retryIfResult(ResultPredicats.isTrue());
                    break;
                case IS_FALSE:
                    retryerBuilder.retryIfResult(ResultPredicats.isFalse());
                    break;
                case IS_ZEOR:
                    retryerBuilder.retryIfResult(ResultPredicats.isZero());
                    break;
                case IS_NEGATIVE:
                    retryerBuilder.retryIfResult(ResultPredicats.isNegative());
                    break;
                case IS_POSITIVE:
                    retryerBuilder.retryIfResult(ResultPredicats.isPositive());
                    break;
                case IS_EMPTY_STRING:
                    retryerBuilder.retryIfResult(ResultPredicats.isEmptyString());
                    break;
                case IS_EMPTY_COLLECTION:
                    retryerBuilder.retryIfResult(ResultPredicats.isEmptyCollection());
                    break;
                case IS_NOT_EMPTY_STRING:
                    retryerBuilder.retryIfResult(ResultPredicats.isNotEmptyString());
                    break;
                case IS_NOT_EMPTY_COLLECTION:
                    retryerBuilder.retryIfResult(ResultPredicats.isNotEmptyCollection());
                    break;
                case IS_EMPTY_MAP:
                    retryerBuilder.retryIfResult(ResultPredicats.isEmptyMap());
                    break;
                case IS_NOT_EMPTY_MAP:
                    retryerBuilder.retryIfResult(ResultPredicats.isNotEmptyMap());
                    break;
                default:
                    break;
            }
        }

        if(!StringUtils.isEmpty(retryIfExpr)) {
            retryerBuilder.retryIfResult(ResultPredicats.isExprTrue(retryIfExpr, parser));
        }

        return retryerBuilder.build();
    }
}
