package cn.ideabuffer.async.test;

import cn.ideabuffer.async.proxy.AsyncProxyUtils;
import cn.ideabuffer.async.test.bean.User;
import cn.ideabuffer.async.test.service.TestUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
        long start = System.currentTimeMillis();
        User user = testUserService.asyncGetUser("sangjian", 29, 0);
        System.out.println("invoke asyncGetUser finished");
        long end = System.currentTimeMillis();
        System.out.println("encode start");
        //User originUser = (User)AsyncProxyUtils.getCglibProxyTargetObject(user);
        byte[] bytes = encode(user);
        System.out.println("encode finished");
        User deserializedUser = (User)decode(bytes);
        System.out.println(deserializedUser.getName());
    }

    public static byte[] encode(Object object) throws Exception {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteArray);
        output.writeObject(object);
        output.flush();
        output.close();
        return byteArray.toByteArray();
    }

    public Object decode(byte[] bytes) throws Exception {
        ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object resultObject = objectIn.readObject();
        objectIn.close();
        return resultObject;
    }

    public static void main(String[] args) throws Exception {
        User user = new User("sangjian", 29);
        encode(user);
    }

}
