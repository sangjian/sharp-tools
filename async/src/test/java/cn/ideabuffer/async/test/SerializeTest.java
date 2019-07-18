package cn.ideabuffer.async.test;

import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.serialize.*;
import cn.ideabuffer.async.test.service.TestUserService;
import cn.ideabuffer.async.test.service.impl.TestUserServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.assertTrue;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
public class SerializeTest {

    @Resource
    private TestUserService testUserService;

    @Test
    public void testJavaSerialize() throws Exception {
        User user = testUserService.asyncGetUser("sangjian", 29, 5000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println("serialize start");
        byte[] bytes = JavaSerializer.serialize(user);
        System.out.println("serialize finished");
        User deserializedUser = (User)JavaDeserializer.deserialize(bytes);
        System.out.println(deserializedUser.getName());
    }

    @Test
    public void testHessianSerialize() throws Exception {
        User user = testUserService.asyncGetUser("sangjian", 29, 5000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println("serialize start");
        byte[] bytes = HessianSerializer.serialize(user);
        System.out.println("serialize finished");
        User deserializedUser = (User)HessianDeserializer.deserialize(bytes);
        System.out.println(deserializedUser.getName());
    }

    @Test
    public void testFastJsonSerialize() throws Exception {
        User user = testUserService.asyncGetUser("sangjian", 29, 5000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println("serialize start");
        byte[] bytes = FastJsonSerializer.serialize(user);
        System.out.println("serialize finished");
        Object obj = FastJsonDeserializer.deserialize(bytes);
        System.out.println(obj);
    }

    @Test
    public void testKryoSerialize() throws Exception {
        User user = testUserService.asyncGetUser("sangjian", 29, 15000);
        System.out.println("invoke asyncGetUser finished");
        System.out.println("serialize start");
        byte[] bytes = KryoSerializer.serialize(user);
        System.out.println("serialize finished");
        Object obj = KryoDeserializer.deserialize(bytes);
        System.out.println(obj);
    }

}
