package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.test.bean.User;

/**
 * @author sangjian.sj
 * @date 2019/07/08
 */
public interface TestThreadLocalService {
    User sleep(int index, long sleep);
}
