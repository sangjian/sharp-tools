package cn.ideabuffer.async.test;

import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.serialize.FastJsonDeserializer;
import cn.ideabuffer.async.test.serialize.FastJsonSerializer;
import cn.ideabuffer.async.test.service.TestPrimitiveService;
import cn.ideabuffer.async.test.service.TestUserService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class AsyncTest {

    @Resource
    private TestUserService testUserService;

    @Resource
    private TestPrimitiveService testPrimitiveService;

    @Resource
    private AsyncTemplate asyncTemplate;

    @Test
    public void testSimpleAsync() throws InterruptedException {

        long start = System.currentTimeMillis();
        User user = testUserService.asyncGetUser("sangjian", 29, 1000);
        System.out.println("invoke asyncGetUser finished, cost:" + (System.currentTimeMillis() - start));
        System.out.println(user.getName());
    }

    /**
     * 测试级联调用
     * @throws InterruptedException
     * @throws IOException
     */
    @Test
    public void testAsyncChain() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        List<User> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            User user = testUserService.asyncGetUser("sangjian", i, 0);
            list.add(user);

        }
        System.out.println(" async finished ===============");
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            System.out.println(user.getAge());
        }
    }

    @Test
    public void testGetUserNull() {
        User user = testUserService.asyncGetUserNull(1000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println(user.getName());
    }

    @Test
    public void testGetUserNullToString() {
        User user = testUserService.asyncGetUserNull(1000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println(user);
    }

    @Test
    public void testGetInt() throws ExecutionException, InterruptedException {
        Future<Integer> future = testPrimitiveService.getInt();
        System.out.println("invoke testGetInt finished");
        System.out.println(future.get());
    }

    @Test
    public void testException() throws ExecutionException, InterruptedException {
        User user = testUserService.getException();
        System.out.println("invoke testException finished");
        System.out.println(user);
    }

    @Test
    public void testVoid() throws ExecutionException, InterruptedException {
        testPrimitiveService.getVoid();
        System.out.println("invoke testVoid finished");
        Thread.sleep(5000);
    }

}
