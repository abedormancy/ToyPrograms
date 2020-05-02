package tool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
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
            e.printStackTrace();
        }
    }

    private static void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 并发执行模拟 (主要用于演示下面几个同步器)
     * @param threads 并行数
     * @param runnable 执行的方法
     */
    public static void execute(int threads, Runnable runnable) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier barrier = new CyclicBarrier(threads); // 障栅，允许线程集等待至其中预定数目的线程到达一个障栅，然后可以选择执行一个动作
        CountDownLatch latch = new CountDownLatch(threads); // 闭锁，允许线程集等待直到计数器为 0
        Semaphore semaphore = new Semaphore(8); // 信号量，允许线程集等待直到被允许继续运行为止，（可控制同时访问的线程个数）

        for (int i = 0; i < threads; i++) {
            pool.execute(() -> {
                // 等待所有的线程就绪后才放行
                await(barrier);
                // 获取许可
                acquire(semaphore);
                // 为演示‘障栏’和‘闭锁’的效果，直接调用 run 方法执行
                runnable.run();
                // 释放许可
                semaphore.release();
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
            IntStream.range(0, 500000).forEach(i -> {
                count++;
                adder.add(1);
            });
        });
        System.out.printf("count: %d\nadder: %d", count, adder.intValue());
    }

    private static LongAdder adder = new LongAdder();
    private static int count = 0;
}