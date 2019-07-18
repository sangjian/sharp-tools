package cn.ideabuffer.async.test;

import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.serialize.FastJsonDeserializer;
import cn.ideabuffer.async.test.serialize.FastJsonSerializer;
import cn.ideabuffer.async.test.service.TestPrimitiveService;
import cn.ideabuffer.async.test.service.TestUserService;

import cn.ideabuffer.async.test.service.impl.TestUserServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class AsyncTest {

    private final Logger logger = LoggerFactory.getLogger(AsyncTest.class);

    @Resource
    private TestUserService testUserService;

    @Resource
    private TestPrimitiveService testPrimitiveService;

    @Resource
    private AsyncTemplate asyncTemplate;

    private ExecutorService executor = Executors.newFixedThreadPool(1);

    @Test
    public void testSimpleAsync() throws InterruptedException {
        long start = System.currentTimeMillis();
        logger.debug("before invoke getUser");
        User user1 = testUserService.getUser("aaa", 11, 2000);
        //User user2 = testUserService.getUser("bbb", 22, 2000);
        logger.debug("after invoke getUser");
        logger.debug("async invoke getUser cost:{}", System.currentTimeMillis() - start);
        logger.debug("user1:{}", user1);
        //logger.debug("user2:{}", user2);
        logger.debug("total cost:{}", System.currentTimeMillis() - start);
    }

    @Test
    public void testAsync() throws InterruptedException, IOException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            User user = testUserService.asyncGetUser("sangjian", i, 0);
            System.out.println(user.getAge());
        }
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
        System.out.println(future.get());
    }

    @Test
    public void testException() throws ExecutionException, InterruptedException {
        User user = testUserService.getException();
        System.out.println("invoke testException finished");
        System.out.println(user);
    }

    @Test
    public void testTimeout() throws ExecutionException, InterruptedException {
        User user = testUserService.getUserTimeout(3000);
        System.out.println("invoke testTimeout finished");
        System.out.println(user);
    }

    @Test
    public void testVoid() throws ExecutionException, InterruptedException {
        testPrimitiveService.getVoid();
        System.out.println("invoke testVoid finished");
        Thread.sleep(5000);
    }

    @Test
    public void testList() throws InterruptedException {
        List<User> list = testUserService.getUserList(5000);
        System.out.println("getUserList finished");
        System.out.println(list);
    }

    @Test
    public void testTransactional() throws InterruptedException {
        User user = testUserService.testTransactional();
        System.out.println("testTransactional finished");
        System.out.println(user);
    }

}
