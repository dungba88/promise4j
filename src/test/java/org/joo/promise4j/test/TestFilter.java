package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SyncDeferredObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestFilter {

    private volatile boolean result = false;

    private Supplier<Deferred<Integer, Throwable>> deferredSupplier;

    public TestFilter(Supplier<Deferred<Integer, Throwable>> deferredSupplier) {
        this.deferredSupplier = deferredSupplier;
    }

    @Test
    public void testDoneFilterException() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).filterDone(response -> {
            return response + 1;
        }).filterDone(response -> {
            return response + "";
        }).done(response -> {
            latch.countDown();
        }).fail(ex -> {
            if (ex instanceof NullPointerException) {
                result = true;
            }
            latch.countDown();
        });

        deferred.reject(new NullPointerException());

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
    }

    @Test
    public void testDonePipeline() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).filterDone(response -> {
            return response + 1;
        }).filterDone(response -> {
            return response + "";
        }).done(response -> {
            if (response.equals("2"))
                result = true;
            latch.countDown();
        }).fail(ex -> {
            latch.countDown();
        });

        deferred.resolve(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
    }

    @Test
    public void testFailPipeline() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).filterFail(ex -> {
            return new IllegalArgumentException();
        }).filterDone(response -> {
            return (int) response + 1;
        }).done(response -> {
            if (response == 2)
                result = true;
            latch.countDown();
        }).fail(ex -> {
            latch.countDown();
        });

        deferred.resolve(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
    }

    @Test
    public void testFilterThrowException() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + 1);
        }).filterDone(response -> {
            throw new IllegalArgumentException();
        }).filterFail(ex -> {
            if (ex instanceof IllegalArgumentException)
                return new IllegalStateException();
            return null;
        }).filterFail(ex -> {
            if (ex instanceof IllegalStateException)
                throw new NullPointerException();
            return null;
        }).done(response -> {
            // should not called here
            latch.countDown();
        }).fail(ex -> {
            // should not called here
            if (ex instanceof NullPointerException)
                result = true;
            latch.countDown();
        });

        deferred.resolve(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
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
