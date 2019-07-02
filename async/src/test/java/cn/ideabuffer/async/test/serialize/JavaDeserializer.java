package cn.ideabuffer.async.test.serialize;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class JavaDeserializer {

    public static Object deserialize(byte[] bytes) throws Exception {
        ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object resultObject = objectIn.readObject();
        objectIn.close();
        return resultObject;
    }
}
