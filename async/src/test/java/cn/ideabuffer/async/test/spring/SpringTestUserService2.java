package cn.ideabuffer.async.test.spring;

import cn.ideabuffer.async.test.bean.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
@Service
public class SpringTestUserService2 {

    @Async
    public Future<User> getUser() {
        return AsyncResult.forValue(new User("sangjian", 29));
    }

}
