package cn.ideabuffer.async.test.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class KryoDeserializer {

    public static Object deserialize(byte[] bytes) throws Exception {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        kryo.setReferences(false);
        Input input = new Input(bytes);
        return kryo.readClassAndObject(input);
    }

}
