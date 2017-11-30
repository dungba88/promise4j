package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.CompletableDeferredObject;
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
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            executor.submit(() -> {
                deferred.resolve(1);
            });
            deferred.promise().done(done -> {
                if (atomicCounter.incrementAndGet() == iterations * 2)
                    latch.countDown();
            }).fail(ex -> {

            }).always((status, response, ex) -> {
                if (atomicCounter.incrementAndGet() == iterations * 2)
                    latch.countDown();
            });
        }

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(iterations * 2, atomicCounter.get());
        long ellapsed = System.currentTimeMillis() - start;
        long pace = iterations * 1000 / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }

    @Test
    public void testResolveWithSyncGetTimeout() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService resolveExecutor = Executors.newFixedThreadPool(7);
        ExecutorService getWaitExecutor = Executors.newFixedThreadPool(7);
        int iterations = 1000000;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            resolveExecutor.submit(() -> {
                deferred.resolve(1);
            });

            getWaitExecutor.submit(() -> {
                try {
                    Assert.assertEquals(1, deferred.promise().get(500, TimeUnit.MILLISECONDS));
                } catch (PromiseException | TimeoutException e) {
                    Assert.fail(e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail(e.getMessage());
                }

                if (atomicCounter.incrementAndGet() == iterations)
                    latch.countDown();
            });
        }

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(iterations, atomicCounter.get());
        long ellapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }

    @Test
    public void testResolveWithSyncGet() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService resolveExecutor = Executors.newFixedThreadPool(7);
        ExecutorService getWaitExecutor = Executors.newFixedThreadPool(7);
        int iterations = 1000000;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            getWaitExecutor.submit(() -> {
                try {
                    Assert.assertEquals(1, deferred.promise().get());
                } catch (PromiseException e) {
                    Assert.fail(e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail(e.getMessage());
                }

                if (atomicCounter.incrementAndGet() == iterations)
                    latch.countDown();
            });
            resolveExecutor.submit(() -> {
                deferred.resolve(1);
            });
        }

        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(iterations, atomicCounter.get());
        long ellapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }
    
    @Test
    public void testInterrupt() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService getWaitExecutor = Executors.newFixedThreadPool(7);
        int iterations = 1000000;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            getWaitExecutor.submit(() -> {
                try {
                    Assert.assertEquals(1, deferred.promise().get());
                } catch (PromiseException e) {
                    Assert.fail(e.getMessage());
                } catch (InterruptedException e) {
                    if (atomicCounter.incrementAndGet() == 7)
                        latch.countDown();
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        getWaitExecutor.shutdownNow();

        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        
        System.out.println(atomicCounter.get());

        Assert.assertEquals(7, atomicCounter.get());
        long ellapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }
    
    @Test
    public void testInterruptTimeout() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService getWaitExecutor = Executors.newFixedThreadPool(7);
        int iterations = 1000000;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            getWaitExecutor.submit(() -> {
                try {
                    Assert.assertEquals(1, deferred.promise().get(1000, TimeUnit.MILLISECONDS));
                } catch (PromiseException e) {
                    Assert.fail(e.getMessage());
                } catch (InterruptedException e) {
                    if (atomicCounter.incrementAndGet() == 7)
                        latch.countDown();
                    Thread.currentThread().interrupt();
                } catch (TimeoutException e) {
                    Assert.fail(e.getMessage());
                }
            });
        }
        
        getWaitExecutor.shutdownNow();

        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        
        Assert.assertEquals(7, atomicCounter.get());
        long ellapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }
    
    @Test
    public void testTimeout() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService getWaitExecutor = Executors.newFixedThreadPool(7);
        int iterations = 100;

        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            final Deferred<Object, Throwable> deferred = deferredSupplier.get();
            getWaitExecutor.submit(() -> {
                try {
                    Assert.assertEquals(1, deferred.promise().get(500, TimeUnit.NANOSECONDS));
                } catch (PromiseException e) {
                    Assert.fail(e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail(e.getMessage());
                } catch (TimeoutException e) {
                    if (atomicCounter.incrementAndGet() == iterations)
                        latch.countDown();
                }
            });
        }
        
        try {
            latch.await(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        
        Assert.assertEquals(iterations, atomicCounter.get());
        long ellapsed = System.nanoTime() - start;
        long pace = iterations * 1000000000L / ellapsed;
        System.out.println("Testing " + deferredSupplier.get().getClass().getName() + " @ " + pace + " ops/sec");
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[] { (Supplier<Deferred<Object, Throwable>>) () -> new AsyncDeferredObject<>() });
        list.add(new Object[] { (Supplier<Deferred<Object, Throwable>>) () -> new SyncDeferredObject<>() });
        list.add(new Object[] { (Supplier<Deferred<Object, Throwable>>) () -> new CompletableDeferredObject<>() });
        return list;
    }
}
