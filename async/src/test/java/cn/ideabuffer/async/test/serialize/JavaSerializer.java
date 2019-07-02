package cn.ideabuffer.async.test.serialize;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class JavaSerializer {

    public static byte[] serialize(Object object) throws Exception {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(byteArray);
        output.writeObject(object);
        output.flush();
        output.close();
        return byteArray.toByteArray();
    }

}
