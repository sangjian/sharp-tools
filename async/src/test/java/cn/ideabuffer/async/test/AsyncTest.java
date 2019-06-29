package cn.ideabuffer.async.test;

import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestUserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

/**
 * @author sangjian.sj
 * @date 2019/06/27
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class AsyncTest {

    @Resource
    private TestUserService testUserService;

    @Test
    public void testAsync() {
        long start = System.currentTimeMillis();
        User user = testUserService.asyncGetUser("sangjian", 29, 5000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println(user);
        long end = System.currentTimeMillis();
        assertTrue(end - start < 8000);
        System.out.println(user.getName());
    }

}
