package Week12HomeWork;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

public class CounterDemo {
static final Object lock = new Object();
    static int plain = 0;
    static final AtomicInteger atomic = new AtomicInteger();
    static final LongAdder adder = new LongAdder();
    static final int THREADS = 8;
    static final int OPERATIONS = 1_000_000;

    private static long measure(Runnable increment)
            throws InterruptedException, ExecutionException {

        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService pool =
                     Executors.newFixedThreadPool(THREADS)) {

            List<Future<?>> futures = new ArrayList<>();

            int perThread = OPERATIONS / THREADS;

            for (int i = 0; i < THREADS; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();

                    for (int j = 0; j < perThread; j++) {
                        increment.run();
                    }

                    return null;
                }));
            }

            ready.await();

            long begin = System.nanoTime();

            start.countDown();

            for (Future<?> future : futures) {
                future.get();
            }

            return System.nanoTime() - begin;
        }
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Object lock = new Object();
        int[] syncCount = {0};

        long syncTime = measure(() -> {
            synchronized (lock) {
                syncCount[0]++;
            }
        });

        AtomicInteger atomic = new AtomicInteger();

        long atomicTime = measure(atomic::incrementAndGet);

        LongAdder adder = new LongAdder();

        long adderTime = measure(adder::increment);

        System.out.printf("synchronized: %.2f ms, count=%d%n",
                syncTime / 1_000_000.0, syncCount[0]);

        System.out.printf("AtomicInteger: %.2f ms, count=%d%n",
                atomicTime / 1_000_000.0, atomic.get());

        System.out.printf("LongAdder: %.2f ms, count=%d%n",
                adderTime / 1_000_000.0, adder.sum());
    }
}