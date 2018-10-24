package tool.java;

import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by Abe on 10/24/2018.
 */
public class ConcurrentMock {

    public static void execute(int threads, Runnable runnable) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads); // 障栏
        CountDownLatch latch = new CountDownLatch(threads); // 闭锁

        Runnable r = (Runnable) Proxy.newProxyInstance(Runnable.class.getClassLoader(), new Class[]{Runnable.class}, (proxy, method, args) -> {
            // 等待所有的线程就绪后才放行
            barrier.await();
            Object result = null;
            result = method.invoke(runnable, args);
            latch.countDown();
            return result;
        });

        for (int i = 0; i < threads; i++) {
            pool.execute(r);
        }
        pool.shutdown();
        // 等待所有线程执行完毕后才返回
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return;
    }

    public static void main(String[] args) {
        ConcurrentMock.execute(10, () -> {
            IntStream.rangeClosed(1, 1000).forEach(i -> {
                count++;
                adder.add(1);
            });
        });
        System.out.printf("count: %d\nadder: %d", count, adder.intValue());
    }

    private static LongAdder adder = new LongAdder();
    private static int count = 0;
}