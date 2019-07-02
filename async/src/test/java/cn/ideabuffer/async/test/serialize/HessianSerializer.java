package cn.ideabuffer.async.test.serialize;

import com.caucho.hessian.io.HessianOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class HessianSerializer {

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = null;
        HessianOutput hessianOutput = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            // Hessian的序列化输出
            hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                hessianOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
