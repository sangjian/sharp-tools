package cn.ideabuffer.async.test.serialize;

import com.caucho.hessian.io.HessianInput;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class HessianDeserializer {

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        HessianInput hessianInput = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            // Hessian的反序列化读取对象
            hessianInput = new HessianInput(byteArrayInputStream);
            return hessianInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                hessianInput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
