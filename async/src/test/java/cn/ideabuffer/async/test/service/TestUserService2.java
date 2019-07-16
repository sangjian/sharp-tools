package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.test.bean.User;

import java.util.List;

/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
public interface TestUserService2 {

    @Async(allowCascade = true)
    User getUser(String name, int age, int sleep);

}
