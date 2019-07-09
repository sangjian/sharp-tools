package cn.ideabuffer.async.test.service.impl;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestThreadLocalService;

/**
 * @author sangjian.sj
 * @date 2019/07/08
 */
public class TestThreadLocalServiceImpl implements TestThreadLocalService {

    //@Async
    @Override
    public User sleep(int index, long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("sleep finished");
        return new User("fdfd", index);
    }

}
