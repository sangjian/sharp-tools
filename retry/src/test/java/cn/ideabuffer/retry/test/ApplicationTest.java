package cn.ideabuffer.retry.test;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author sangjian.sj
 * @date 2019/05/23
 */
public class ApplicationTest {

    @Test
    public void start() {
        ApplicationContext context = new ClassPathXmlApplicationContext("application-test.xml");
        TestRetry testRetry = (TestRetry)context.getBean("testRetry");
        System.out.println(testRetry.testRetryIfExpr());
    }
}
