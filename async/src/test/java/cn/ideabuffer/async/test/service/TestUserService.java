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
    public User getUser(String name, int age, int sleep) {
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


}
