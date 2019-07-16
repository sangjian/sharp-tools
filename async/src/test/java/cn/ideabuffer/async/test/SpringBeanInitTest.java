package cn.ideabuffer.async.test;

import cn.ideabuffer.async.test.spring.SpringTestInitMethodService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author sangjian.sj
 * @date 2019/07/15
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class SpringBeanInitTest {

    @Resource
    private SpringTestInitMethodService springTestInitMethodService;

    @Test
    public void test() {
        long start = System.currentTimeMillis();
        springTestInitMethodService.test();
        long end = System.currentTimeMillis();
        System.out.println("total cost:" + (end - start));
    }

}
