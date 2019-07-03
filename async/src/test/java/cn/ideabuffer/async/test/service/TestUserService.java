package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.test.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
@Service
public class TestUserService {

    private static Logger logger = LoggerFactory.getLogger(TestUserService.class);

    @Async
    public User asyncGetUser(String name, int age, int sleep) {
        logger.info("enter asyncGetUser");
        User user = getUser(name, age, 0);
        logger.info("asyncGetUser invoke getUser finished");
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return user;
    }

    //@Async
    private User getUser(String name, int age, int sleep) {
        logger.info("enter getUser");
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new User(name, age);
    }

    @Async
    public User asyncGetUserNull(int sleep) {
        logger.info("enter asyncGetUserNull");

        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Async
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

    @Async
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

    @Async
    public User getException() {
        System.out.println("in getAsyncUserPrivate");
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        throw new NullPointerException("in getException");
    }

}
