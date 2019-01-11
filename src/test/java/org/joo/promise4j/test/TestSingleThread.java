package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.JoinedPromise;
import org.joo.promise4j.impl.JoinedResults;
import org.joo.promise4j.impl.SyncDeferredObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSingleThread {

    private Supplier<Deferred<Object, Throwable>> deferredSupplier;

    public TestSingleThread(Supplier<Deferred<Object, Throwable>> deferredSupplier) {
        this.deferredSupplier = deferredSupplier;
    }

    @Test
    public void testResolveBefore() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.resolve(1);
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.RESOLVED, status);
            Assert.assertNull(ex);
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        });
        Assert.assertEquals(2, counter.get());

        try {
            Assert.assertEquals(1, deferred.promise().get());
        } catch (PromiseException e) {
            Assert.fail(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        try {
            Assert.assertEquals(1, deferred.promise().get(1000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException | TimeoutException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testResolveAfter() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.RESOLVED, status);
            Assert.assertNull(ex);
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        });
        deferred.resolve(1);
        Assert.assertEquals(2, counter.get());

        try {
            Assert.assertEquals(1, deferred.promise().get());
        } catch (PromiseException e) {
            Assert.fail(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        }
        try {
            Assert.assertEquals(1, deferred.promise().get(1000, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException | TimeoutException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testRejectBefore() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.reject(new UnsupportedOperationException());
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.REJECTED, status);
            Assert.assertNull(response);
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        });
        Assert.assertEquals(2, counter.get());

        try {
            deferred.promise().get();
            Assert.fail("must fail");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }

        try {
            deferred.promise().get(1000, TimeUnit.MILLISECONDS);
            Assert.fail("must fail");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedOperationException);
        } catch (TimeoutException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testRejectAfter() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.REJECTED, status);
            Assert.assertNull(response);
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        });
        deferred.reject(new UnsupportedOperationException());
        Assert.assertEquals(2, counter.get());

        try {
            deferred.promise().get();
            Assert.fail("must fail");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }

        try {
            deferred.promise().get(1000, TimeUnit.MILLISECONDS);
            Assert.fail("must fail");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Assert.fail(e.getMessage());
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedOperationException);
        } catch (TimeoutException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testResolveTwice() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.RESOLVED, status);
            Assert.assertNull(ex);
            Assert.assertEquals(1, response);
            counter.incrementAndGet();
        });
        deferred.resolve(1);
        Assert.assertEquals(2, counter.get());
        try {
            deferred.resolve(1);
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }
    }

    @Test
    public void testRejectTwice() {
        AtomicInteger counter = new AtomicInteger(0);
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.REJECTED, status);
            Assert.assertNull(response);
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
            counter.incrementAndGet();
        });
        deferred.reject(new UnsupportedOperationException());
        Assert.assertEquals(2, counter.get());
        try {
            deferred.reject(new UnsupportedOperationException());
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testJoinedPromise() {
        List<Promise<Object, Throwable>> deferreds = createDeferreds();
        testJoinedPromise(JoinedPromise.of(deferreds.toArray(new Promise[0])), deferreds);

        deferreds = createDeferreds();
        testJoinedPromise(JoinedPromise.of(deferreds.toArray(new Deferred[0])), deferreds);

        deferreds = createDeferreds();
        testJoinedPromise(JoinedPromise.of(deferreds), deferreds);
    }

    @SuppressWarnings("unchecked")
    private List<Promise<Object, Throwable>> createDeferreds() {
        List<Promise<Object, Throwable>> deferreds = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            deferreds.add((Promise<Object, Throwable>) deferredSupplier.get());
        return deferreds;
    }

    @SuppressWarnings("unchecked")
    private void testJoinedPromise(Promise<JoinedResults<Object>, Throwable> promise,
            List<Promise<Object, Throwable>> deferreds) {
        CountDownLatch latch = new CountDownLatch(1);
        promise.done(results -> {
            Assert.assertEquals(deferreds.size(), results.size());
            for (int i = 0; i < results.size(); i++)
                Assert.assertEquals(i, results.get(i));
            for (Object result : results)
                Assert.assertTrue(result instanceof Integer);
            latch.countDown();
        });
        for (int i = 0; i < deferreds.size(); i++) {
            ((Deferred<Object, Throwable>) deferreds.get(i)).resolve(i);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testTimeout() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        CountDownLatch latch = new CountDownLatch(1);
        deferred.promise().fail(ex -> {
            if (ex instanceof TimeoutException) {
                latch.countDown();
            }
        });
        deferred.withTimeout(1000, TimeUnit.MILLISECONDS, () -> new TimeoutException());
        try {
            Thread.sleep(2000);
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }
        deferred.resolve(1);
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
