package cn.ideabuffer.async.test.service.impl;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.test.service.TestTransactionService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sangjian.sj
 * @date 2019/07/07
 */
public class TestTransactionServiceImpl implements TestTransactionService {

    @Transactional
    @Override
    public void testTransaction() {

    }

    @Async
    @Override
    public void async() {

    }

}
