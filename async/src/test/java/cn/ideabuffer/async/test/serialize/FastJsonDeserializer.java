package cn.ideabuffer.async.test.serialize;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class FastJsonDeserializer {

    public static Object deserialize(byte[] bytes) {
        return JSON.parse(new String(bytes));
    }

}
