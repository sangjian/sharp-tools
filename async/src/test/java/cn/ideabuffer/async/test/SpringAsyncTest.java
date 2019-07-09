package cn.ideabuffer.async.test;

import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.spring.SpringTestUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class SpringAsyncTest {

    @Resource
    private SpringTestUserService springTestUserService;

    @Test
    public void testGetUser() throws ExecutionException, InterruptedException {
        Future<User> future = springTestUserService.getUser();
        System.out.println("getUser finished, future:" + future.getClass().getName());
        System.out.println(future.get());
    }

    @Test
    public void testUser() throws ExecutionException, InterruptedException {
        springTestUserService.testUser();
        System.out.println("testUser finished");
        Thread.sleep(5000);
    }

    /**
     * 存在死锁问题
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testUserChain() throws ExecutionException, InterruptedException {
        List<Future<User>> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Future<User> future = springTestUserService.getUser();
            list.add(future);
            //System.out.println(future.get());
        }

        for (int i = 0; i < 1000; i++) {
            System.out.println(list.get(i).get());
        }
        System.out.println("testUser finished");
        Thread.sleep(5000);
    }

}
