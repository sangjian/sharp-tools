package cn.ideabuffer.async.test.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class KryoSerializer {

    public static byte[] serialize(Object object) {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        Output output = new Output(1024);
        kryo.writeClassAndObject(output, object);
        return output.toBytes();
    }

}
