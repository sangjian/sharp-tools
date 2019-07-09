package cn.ideabuffer.async.test;

import cn.ideabuffer.async.core.AsyncCallable;
import cn.ideabuffer.async.core.AsyncExecutor;
import cn.ideabuffer.async.core.AsyncProxyResultSupport;
import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestThreadLocalService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/06/29
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class ThreadLocalTest {

    private static final ThreadLocal<String> t1 = new ThreadLocal<>();

    private AsyncExecutor executor = new AsyncExecutor(1);

    @Resource
    private AsyncTemplate asyncTemplate;

    @Resource
    private TestThreadLocalService testThreadLocalService;

    @Test
    public void testThreadLocal() throws ExecutionException, InterruptedException {
        ThreadLocal<String> threadLocal = new ThreadLocal<>();
        threadLocal.set("test-value");

        AsyncCallable callable = new AsyncCallable() {
            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public Object call() throws Exception {
                System.out.println(Thread.currentThread().getName() + "," + threadLocal.get());
                return null;
            }
        };

        Future future = executor.submit(callable);

        future.get();

        threadLocal.set("test-value-2");

        executor.submit(callable);


    }

    @Test
    public void testInheritableThreadLocal() throws ExecutionException, InterruptedException {
        InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<>();
        threadLocal.set("test-inheritable-value");

        AsyncCallable callable = new AsyncCallable() {
            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public Object call() throws Exception {
                System.out.println(Thread.currentThread().getName() + "," + threadLocal.get());
                return null;
            }
        };

        Future future = executor.submit(callable);

        future.get();

        threadLocal.set("test-inheritable-value-2");

        executor.submit(callable);

    }

    @Test
    public void testSetThreadLocal() throws ExecutionException, InterruptedException {
        executor.init();
        ThreadLocal<String> threadLocal = new ThreadLocal<>();

        AsyncCallable callable = new AsyncCallable() {
            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public Object call() throws Exception {
                System.out.println(Thread.currentThread().getName() + "," + threadLocal.get());
                threadLocal.set("test-in-call-value");
                return null;
            }
        };

        Future future = executor.submit(callable);
        future.get();
        System.out.println(threadLocal.get());
    }

    @Test
    public void testCglibCallbackThreadLocal() throws InterruptedException {
        User user1 = asyncTemplate.submit((Callable<User>)() -> testThreadLocalService.sleep(1, 10000), User.class);
        Thread.sleep(1000);
        //System.out.println(((AsyncProxyResultSupport)user1)._getFuture());
        Thread thread = Thread.currentThread();
        User user2 = asyncTemplate.submit((Callable<User>)() -> testThreadLocalService.sleep(2, 10000), User.class);

        Thread.sleep(1000);
        System.out.println(((AsyncProxyResultSupport)user2)._getFuture());
        System.out.println(((AsyncProxyResultSupport)user1)._getFuture());
        Thread.sleep(1000);
        System.out.println(user1.getAge());
        Thread.sleep(1000);

        System.out.println(user2.getAge());
    }

    @Test
    public void testThreadLocalMain() {

        t1.set("t1");
        t1.set("t2");
        Thread thread = Thread.currentThread();
        System.out.println();

    }

}
