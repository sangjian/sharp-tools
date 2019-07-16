package cn.ideabuffer.async.test.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author sangjian.sj
 * @date 2019/07/15
 */
public class SpringTestInitMethodService implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(SpringTestInitMethodService.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("in afterPropertiesSet before sleep");
        Thread.sleep(10000);
        logger.debug("in afterPropertiesSet after sleep");
    }


    public void init() throws InterruptedException {
        logger.debug("in init before sleep");
        Thread.sleep(20000);
        logger.debug("in init after sleep");
    }

    public void test(){
        logger.debug("in test");
    }
}
