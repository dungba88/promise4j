package org.joo.promise4j.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.junit.Assert;
import org.junit.Test;

public class TestMultiCallback {

    @Test
    public void test() {
        ExecutorService executor = Executors.newFixedThreadPool(7);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        int iterations = 1000000;
        
        long start = System.currentTimeMillis();
        for(int i=0; i<iterations; i++) {
            final Deferred<Object, Throwable> deferred = new CompletableDeferredObject<>();
            executor.submit(() -> {
                deferred.resolve(1);
            });
            deferred.promise().done(done -> {
                if (atomicCounter.incrementAndGet() == iterations * 2) {
                    latch.countDown();
                }
            }).done(done -> {
                if (atomicCounter.incrementAndGet() == iterations * 2) {
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
        long pace = iterations * 2 * 1000 / ellapsed;
        System.out.println("Testing multi-callbacks @ " + pace + " ops/sec");
    }
}
