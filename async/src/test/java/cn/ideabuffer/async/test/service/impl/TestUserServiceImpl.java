package cn.ideabuffer.async.test.service.impl;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestUserService;
import cn.ideabuffer.async.test.service.TestUserService2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sangjian.sj
 * @date 2019/07/07
 */
public class TestUserServiceImpl {
    private static Logger logger = LoggerFactory.getLogger(TestUserServiceImpl.class);

    @Resource
    private AsyncTemplate asyncTemplate;

    @Resource
    private TestUserService2 testUserService2;

    private String test;

    //@Override
    public User asyncGetUser(String name, int age, int sleep) {
        System.out.println(String.format("time:%d\tenter asyncGetUser\tthread:%s", System.currentTimeMillis(), Thread.currentThread().getName()));
        //User user = asyncTemplate.submit(() -> getUser(name, age, sleep), User.class);
        User user = testUserService2.getUser(name, age, sleep);
        System.out.println(String.format("time:%d\tasyncGetUser invoke getUser finished\tuserClass:%s", System.currentTimeMillis(), user.getClass().getName()));
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("asyncGetUser sleep finished, Thread:{}", Thread.currentThread().getName());

        return user;
    }

    //@Override
    public User getUser(String name, int age, int sleep) {
        try {

            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("getUser sleep finished, Thread:{}", Thread.currentThread().getName());
        return new User(name, age);
    }

    //@Override
    public User asyncGetUserNull(int sleep) {
        logger.info("enter asyncGetUserNull");

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    //@Override
    public User getUserPrivate(String name, int age) {
        System.out.println("getUserPrivate thread:" + Thread.currentThread().getName());
        User user = getAsyncUserPrivate(name, age);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("getUserPrivate sleep finished");
        return user;
    }

    private User getAsyncUserPrivate(String name, int age) {
        System.out.println("in getAsyncUserPrivate");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("getAsyncUserPrivate finished, thread:" + Thread.currentThread().getName());
        return new User(name, age);
    }

    //@Override
    public User getException() {
        System.out.println("in getAsyncUserPrivate");
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("in getException");
    }

    //@Override
    public User asyncGetUser(int sleep) {
        logger.info("enter asyncGetUser");

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new User("sangjian", 29);
    }

    //@Override
    public List<User> getUserList(int sleep) throws InterruptedException {
        if(sleep > 0) {
            Thread.sleep(sleep);
        }
        List<User> list = new ArrayList<>();
        list.add(new User("aaa", 12));
        return list;
    }

    @Async(timeout = 1000)
    //@Override
    public User getUserTimeout(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new User("test", 1);
    }

    @Transactional
    //@Override
    public User testTransactional() {
        return null;
    }
}
