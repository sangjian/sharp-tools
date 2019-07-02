package cn.ideabuffer.async.test.serialize;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;

/**
 * @author sangjian.sj
 * @date 2019/07/02
 */
public class FastJsonSerializer {

    public static byte[] serialize(Object obj) throws UnsupportedEncodingException {
        String text = JSON.toJSONString(obj);
        return text.getBytes("UTF-8");
    }

}
