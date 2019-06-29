package cn.ideabuffer.retry.test;

import cn.ideabuffer.retry.annotation.BackOff;
import cn.ideabuffer.retry.annotation.Retry;
import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * @author sangjian.sj
 * @date 2019/05/23
 */
@Component
public class TestRetry {

    private Random random = new Random();

    @Before
    public void start() {
        ApplicationContext context = new ClassPathXmlApplicationContext("application-test.xml");
        //TestRetry testRetry = (TestRetry)context.getBean("testRetry");
        //System.out.println(testRetry.testRetryIfResultZero());
    }

    @Test
    public void test() {
        testRetryIfResultZero();
    }

    @Retry(
        maxAttempts = 15,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_ZEOR},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.EXPONENTIAL_WAIT))
    public int testRetry() {
        System.out.println("in testRetry, " + System.currentTimeMillis() % 100000);
        int next = random.nextInt();
        if(next % 100 != 1) {
            throw new NullPointerException();
        }
        return 0;
    }

    @Retry(
        maxAttempts = 15,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_EMPTY_STRING},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.EXPONENTIAL_WAIT))
    public String testRetryIfResultEmpty() {
        System.out.println("in testRetryIfResultEmpty");
        return null;
    }

    @Retry(
        maxAttempts = 3,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_EMPTY_COLLECTION},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.EXPONENTIAL_WAIT))
    public List<Long> testRetryIfResultCollectionEmpty() {
        System.out.println("in testRetryIfResultCollectionEmpty");
        return new ArrayList<Long>(){{
            add(1L);
        }};
    }

    @Retry(
        maxAttempts = 3,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_NOT_EMPTY_COLLECTION},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.EXPONENTIAL_WAIT))
    public List<Long> testRetryIfResultCollectionNotEmpty() {
        System.out.println("in testRetryIfResultCollectionNotEmpty");
        return new ArrayList<Long>(){{
            add(1L);
        }};
    }

    @Retry(
        maxAttempts = 3,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_NEGATIVE},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.RANDOM_WAIT))
    public float testRetryIfResultNegative() {
        System.out.println("in testRetryIfResultNegative");
        return -0.000000000000000000000000000000000000000000001f;
    }

    @Retry(
        maxAttempts = 3,
        retryIfExceptionOfType = {IOException.class},
        retryIfRuntimeException = true,
        retryIfResult = {Retry.RetryResult.IS_ZEOR},
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.RANDOM_WAIT))
    public int testRetryIfResultZero() {
        System.out.println("in testRetryIfResultZero");
        return 0;
    }

    @Retry(
        maxAttempts = 3,
        retryIfExceptionOfType = {IOException.class},
        retryIfExpr = "#retVal.getName().equals('sangjian')",
        stopStrategy = Retry.StopStrategy.STOP_AFTER_ATTEMT,
        backOff = @BackOff(
            expMaxTime = 5000,
            waitStrategy = BackOff.WaitStrategy.RANDOM_WAIT))
    public TestObject testRetryIfExpr() {
        System.out.println("in testRetryIfExpr");
        TestObject object = new TestObject();
        object.setName("sangjian");
        object.setId(2);
        return object;
    }

    @Test
    public void test2() {
        Callable<List<Long>> callable = () -> {
            System.out.println("testestet");
            throw new NumberFormatException();
        };

        Retryer<List<Long>> retryer = RetryerBuilder.<List<Long>>newBuilder()
            .retryIfResult(Predicates.in(new ArrayList<>()))
            .retryIfExceptionOfType(IOException.class)
            .retryIfExceptionOfType(NumberFormatException.class)
            .retryIfExceptionOfType(NullPointerException.class)
            .withStopStrategy(StopStrategies.stopAfterAttempt(0))
            .build();
        try {
            retryer.call(callable);
        } catch (RetryException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
