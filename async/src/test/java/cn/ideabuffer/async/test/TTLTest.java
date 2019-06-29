package cn.ideabuffer.async.test;

import com.alibaba.ttl.TransmittableThreadLocal;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sangjian.sj
 * @date 2019/06/29
 */
public class TTLTest {

    @Test
    public void threadPoolTest() {
        ThreadLocal<String> parent = new ThreadLocal<String>();
        parent.set("value-set-in-parent");
        // (1) 抓取当前线程的所有TTL值
        final Object captured = TransmittableThreadLocal.Transmitter.capture();
        System.out.println(parent.get());
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(() -> {
            // (2) 在线程 B中回放在capture方法中抓取的TTL值，并返回 回放前TTL值的备份
            final Object backup = TransmittableThreadLocal.Transmitter.replay(captured);
            System.out.println("in runnable, value:" + parent.get());
            // (3) 恢复线程 B执行replay方法之前的TTL值（即备份）
            TransmittableThreadLocal.Transmitter.restore(backup);
        });
    }

}
