package org.joo.promise4j.test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCompletable {

    private boolean result = false;
    
    @Test
    public void testExternalFuture() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Object> future = new CompletableFuture<>();
        final Deferred<Object, Throwable> deferred = new CompletableDeferredObject<>(future);
        deferred.promise().done(response -> {
            if (response.equals(1))
                result = true;
            latch.countDown();
        });
        future.complete(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertTrue(result);
    }
    
    @Test
    public void testMultiCallback() {
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
            deferred.promise().done(response -> {
                if (atomicCounter.incrementAndGet() == iterations * 2) {
                    latch.countDown();
                }
            }).done(response -> {
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
