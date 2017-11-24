package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.SyncDeferredObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestMultiThread {

    private Supplier<Deferred<Object, Throwable>> deferredSupplier;
    
    public TestMultiThread(Supplier<Deferred<Object, Throwable>> deferredSupplier) {
        this.deferredSupplier = deferredSupplier;
    }

    @Test
    public void testResolveOnExecutor() {
        ExecutorService executor = Executors.newFixedThreadPool(7);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        int iterations = 1000000;
        
        long start = System.currentTimeMillis();
        for(int i=0; i<iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            executor.submit(() -> {
                deferred.resolve(1);
            });
            deferred.promise().done(done -> {
                if (atomicCounter.incrementAndGet() == iterations) {
                    latch.countDown();
                }
            }).fail(ex -> {
                Assert.fail();
            });
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        long ellapsed = System.currentTimeMillis() - start;
        long pace = iterations * 1000 / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[] {(Supplier<Deferred<Object, Throwable>>) () -> new AsyncDeferredObject<>()});
        list.add(new Object[] {(Supplier<Deferred<Object, Throwable>>) () -> new SyncDeferredObject<>()});
        return list;
    }
}
