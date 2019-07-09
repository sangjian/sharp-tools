package cn.ideabuffer.async.test.spring;

import cn.ideabuffer.async.test.bean.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
@Service
public class SpringTestUserService {

    @Resource
    private SpringTestUserService2 springTestUserService2;

    @Async
    public Future<User> getUser() {
        System.out.println("in getUser, thread:" + Thread.currentThread().getName());

        return springTestUserService2.getUser();
    }

    @Async
    public void testUser() {
        System.out.println("in testUser, thread:" + Thread.currentThread().getName());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("in testUser, finished");
    }

}
