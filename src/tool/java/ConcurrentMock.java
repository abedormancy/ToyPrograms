package tool.java;

import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Created by Abe on 10/24/2018.
 */
public class ConcurrentMock {

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @param threads 并行数
     * @param runnable 执行的方法
     */
    public static void execute(int threads, Runnable runnable) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads); // 障栏
        CountDownLatch latch = new CountDownLatch(threads); // 闭锁

        /*Runnable r = (Runnable) Proxy.newProxyInstance(Runnable.class.getClassLoader(), new Class[]{Runnable.class}, (proxy, method, args) -> {
            barrier.await();
            Object result = null;
            result = method.invoke(runnable, args);
            latch.countDown();
            return result;
        });*/

        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                // 等待所有的线程就绪后才放行
                await(barrier);
                // 为演示‘障栏’和‘闭锁’的效果，直接调用 run 方法执行
                runnable.run();
                latch.countDown();
            });
        }
        pool.shutdown();
        // 等待所有线程执行完毕后才返回
        await(latch);
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