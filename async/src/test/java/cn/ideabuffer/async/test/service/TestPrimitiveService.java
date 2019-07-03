package cn.ideabuffer.async.test.service;

import cn.ideabuffer.async.annotation.Async;
import cn.ideabuffer.async.core.AsyncResultFuture;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author sangjian.sj
 * @date 2019/07/03
 */
@Service
public class TestPrimitiveService {

    @Async
    public Future<Integer> getInt() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return new AsyncResultFuture<>(1);
    }

}
