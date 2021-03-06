package cn.ideabuffer.async.test;

import cn.ideabuffer.async.core.AsyncCallable;
import cn.ideabuffer.async.core.AsyncCallback;
import cn.ideabuffer.async.core.AsyncExecutor;
import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * @author sangjian.sj
 * @date 2019/07/01
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class AsyncTemplateTest {

    @Resource
    private AsyncTemplate asyncTemplate;

    @Test
    public void testNormalReturnType() {
        User user = asyncTemplate.submit(new AsyncCallable<User>() {

            @Override
            public User call() throws Exception {

                System.out.println("in call");
                Thread.sleep(4000);
                User user = new User("sangjian", 29);

                return user;
            }

            @Override
            public long getTimeout() {
                return 0;
            }
        }, User.class);
        System.out.println("submit finished");
        System.out.println(user.getName());
    }

    @Test
    public void testNormalReturnTypeCallback() throws InterruptedException, IOException {
        User user = asyncTemplate.submit(() -> {
            System.out.println("in call");
            Thread.sleep(4000);
            return new User("aa", 11);
        }, new AsyncCallback<User>() {
            @Override
            public void onSuccess(User result) {
                System.out.println("in callback, thread:" + Thread.currentThread().getName());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("callback success");
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("callback failed");
            }


        }, User.class);
        System.out.println("submit finished");
        System.out.println(user.getName());
    }

    @Test
    public void testPrimitiveType() {
        Integer value = asyncTemplate.submit(new AsyncCallable<Integer>() {
            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public Integer call() throws Exception {
                System.out.println("in call");
                Thread.sleep(4000);
                System.out.println("sleep finished");
                return 1;
            }
        }, Integer.class);

        System.out.println("submit finished");
        System.out.println(value);
    }

    @Test
    public void testArrayType() throws ExecutionException, InterruptedException {
        Integer[] array = asyncTemplate.submit(new AsyncCallable<Integer[]>() {
            @Override
            public long getTimeout() {
                return 0;
            }

            @Override
            public Integer[] call() throws Exception {
                System.out.println("in call");
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("call finished");
                return new Integer[]{1,2,3};
            }
        }, Integer[].class);


        System.out.println("submit finished");
        System.out.println(array[1]);
    }

}
