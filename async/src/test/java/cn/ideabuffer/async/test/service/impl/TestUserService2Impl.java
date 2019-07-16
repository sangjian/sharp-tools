package cn.ideabuffer.async.test.service.impl;

import cn.ideabuffer.async.core.AsyncTemplate;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestUserService;
import cn.ideabuffer.async.test.service.TestUserService2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sangjian.sj
 * @date 2019/07/07
 */
public class TestUserService2Impl implements TestUserService2 {
    private static Logger logger = LoggerFactory.getLogger(TestUserService2Impl.class);

    @Resource
    private AsyncTemplate asyncTemplate;

    private String test;


    @Override
    public User getUser(String name, int age, int sleep) {
        try {

            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("getUser sleep finished, Thread:{}", Thread.currentThread().getName());
        return new User(name, age);
    }

}
