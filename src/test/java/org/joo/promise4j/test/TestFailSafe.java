package org.joo.promise4j.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.FailSafePromise;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;
import org.junit.Assert;
import org.junit.Test;

import net.jodah.failsafe.ExecutionContext;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class TestFailSafe {

    private volatile boolean result;

    @Test
    public void testRetryException() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);
        FailSafePromise.from(this::trySomethingException, Failsafe.with(retryPolicy).with(executor)).fail(ex -> {
            result = true;
            latch.countDown();
        });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    @Test
    public void testRetryAfter3Exception() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);
        FailSafePromise.from(this::trySomethingAfter3Exception, Failsafe.with(retryPolicy).with(executor))
                .done(response -> {
                    result = true;
                    latch.countDown();
                });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    @Test
    public void testRetryAlwaysFail() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);
        FailSafePromise.fromPromise(this::trySomethingAlwaysFail, Failsafe.with(retryPolicy).with(executor))
                .fail(ex -> {
                    result = true;
                    latch.countDown();
                });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    @Test
    public void testRetrySuccess() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);
        FailSafePromise.fromPromise(this::trySomethingAsync, Failsafe.with(retryPolicy).with(executor))
                .done(response -> {
                    result = true;
                    latch.countDown();
                });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    @Test
    public void testRetryMiss() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(UnsupportedOperationException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);
        FailSafePromise.fromPromise(this::trySomethingAsync, Failsafe.with(retryPolicy).with(executor)).fail(ex -> {
            result = true;
            latch.countDown();
        });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    @Test
    public void testRetryFail() {
        result = false;
        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
                .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(2);
        FailSafePromise.fromPromise(this::trySomethingAsync, Failsafe.with(retryPolicy).with(executor)).fail(ex -> {
            result = true;
            latch.countDown();
        });
        try {
            latch.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assert.assertTrue(result);
    }

    private Promise<Integer, Exception> trySomethingAsync(ExecutionContext executionContext) {
        int retries = executionContext.getExecutions();
        System.out.println("Retries: " + retries);
        if (retries == 3)
            return new SimpleDonePromise<>(1);
        return new SimpleFailurePromise<>(new IllegalStateException());
    }

    private Promise<Integer, Exception> trySomethingAlwaysFail() {
        System.out.println("Retrying");
        return new SimpleFailurePromise<>(new IllegalStateException());
    }

    private Integer trySomethingException() {
        System.out.println("Retrying");
        throw new IllegalStateException();
    }

    private Integer trySomethingAfter3Exception(ExecutionContext executionContext) {
        int retries = executionContext.getExecutions();
        System.out.println("Retries: " + retries);
        if (retries < 3)
            throw new IllegalStateException();
        return 1;
    }
}
