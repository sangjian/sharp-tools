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

    @Async(allowCascade = false)
    User asyncGetUser(String name, int age, int sleep);

    @Async
    User getUser(String name, int age, int sleep);

    @Async
    User asyncGetUserNull(int sleep);

    @Async
    User getUserPrivate(String name, int age);

    @Async
    User getException();

    @Async
    User asyncGetUser(int sleep);

    @Async
    List<User> getUserList(int sleep) throws InterruptedException;

    User getUserTimeout(int sleep);

    @Async
    @Transactional
    User testTransactional();
}
