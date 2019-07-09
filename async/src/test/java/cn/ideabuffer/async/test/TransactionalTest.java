package cn.ideabuffer.async.test;

import cn.ideabuffer.async.test.service.TestTransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author sangjian.sj
 * @date 2019/07/07
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class TransactionalTest {

    @Resource
    private TestTransactionService testTransactionService;

    @Test
    public void testTransaction() {
        System.out.println(testTransactionService.getClass());
    }

}
