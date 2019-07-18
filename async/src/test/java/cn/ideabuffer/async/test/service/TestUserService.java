package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.test.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
public interface TestUserService {

    User asyncGetUser(String name, int age, int sleep);

    User getUser(String name, int age, int sleep);

    User asyncGetUserNull(int sleep);

    User getUserPrivate(String name, int age);

    User getException();

    User asyncGetUser(int sleep);

    List<User> getUserList(int sleep) throws InterruptedException;

    @Async(timeout = 1000)
    User getUserTimeout(int sleep);

    User testTransactional();
}
