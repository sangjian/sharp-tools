package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sangjian.sj
 * @date 2019/07/07
 */

public interface TestTransactionService {
    @Transactional
    void testTransaction();

    @Async
    void async();
}
