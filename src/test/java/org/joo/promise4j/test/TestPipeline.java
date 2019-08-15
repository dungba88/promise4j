package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;
import org.joo.promise4j.impl.SyncDeferredObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestPipeline {

    private volatile boolean result = false;

    private Supplier<Deferred<Integer, Throwable>> deferredSupplier;

    public TestPipeline(Supplier<Deferred<Integer, Throwable>> deferredSupplier) {
        this.deferredSupplier = deferredSupplier;
    }

    @Test
    public void testPipelineFailPropagation() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof NullPointerException);
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + 1);
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + "");
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
    public void testDonePipelineException() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + 1);
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + "");
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
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + 1);
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + "");
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
        }).pipeFail(ex -> {
            return new SimpleFailurePromise<>(new IllegalArgumentException());
        }).pipeDone(response -> {
            return new SimpleDonePromise<>((int) response + 1);
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
    public void testPipelineAlways() throws PromiseException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().then((s, r, e) -> {
            latch.countDown();
            result = (r == 1);
            return Promise.of(2);
        });

        deferred.resolve(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
        Assert.assertEquals(Integer.valueOf(1), deferred.promise().get());
    }

    @Test
    public void testPipelineAlwaysFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        Promise<Integer, Throwable> promise = deferred.promise().then((s, r, e) -> {
            latch.countDown();
            result = (r == 1);
            return Promise.ofCause(new RuntimeException("test"));
        });

        deferred.resolve(1);

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
        try {
            promise.get();
            Assert.fail("must fail");
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof RuntimeException && e.getCause().getMessage().equals("test"));
        }
    }

    @Test
    public void testPipelineAlwaysReject() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        Promise<Integer, Throwable> promise = deferred.promise().then((s, r, e) -> {
            latch.countDown();
            result = e.getMessage().equals("hello");
            return Promise.ofCause(new RuntimeException("test"));
        });

        deferred.reject(new RuntimeException("hello"));

        try {
            latch.await();
        } catch (InterruptedException e) {
            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(result);
        try {
            promise.get();
            Assert.fail("must fail");
        } catch (PromiseException e) {
            Assert.assertTrue(e.getCause() instanceof RuntimeException && e.getCause().getMessage().equals("test"));
        }
    }

    @Test
    public void testDonePipelineMixed() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).pipeDone(response -> {
            return new SimpleFailurePromise<>(new IllegalArgumentException());
        }).pipeDone(response -> {
            // should not called here
            return new SimpleDonePromise<>(1);
        }).done(response -> {
            latch.countDown();
        }).fail(ex -> {
            if (ex instanceof IllegalArgumentException)
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

    @Test
    public void testFailPipelineMixed() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).pipeFail(ex -> {
            return new SimpleDonePromise<>(3);
        }).done(response -> {
            if (response == 3)
                result = true;
            latch.countDown();
        }).fail(ex -> {
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
    public void testPipelineThrowException() {
        CountDownLatch latch = new CountDownLatch(1);
        result = false;

        final Deferred<Integer, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response.intValue());
        }).pipeDone(response -> {
            return new SimpleDonePromise<>(response + 1);
        }).pipeDone(response -> {
            throw new IllegalArgumentException();
        }).pipeFail(ex -> {
            if (ex instanceof IllegalArgumentException)
                return new SimpleFailurePromise<>(new IllegalStateException());
            return null;
        }).pipeFail(ex -> {
            if (ex instanceof IllegalStateException)
                throw new NullPointerException();
            return null;
        }).done(response -> {
            latch.countDown();
        }).fail(ex -> {
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
