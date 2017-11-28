package org.joo.promise4j.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.joo.promise4j.Promise;

import net.jodah.failsafe.AsyncFailsafe;
import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.ExecutionContext;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

public class FailSafePromise<D, F extends Throwable> extends CompletableDeferredObject<D, F> {

    private FailSafePromise(final CompletableFuture<D> future) {
        super(future);
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(Callable<Promise<D, F>> callable,
            ScheduledExecutorService executor, RetryPolicy retryPolicy) {
        return from(callable, Failsafe.with(retryPolicy).with(executor));
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(Callable<Promise<D, F>> callable,
            ScheduledExecutorService executor, RetryPolicy retryPolicy, CircuitBreaker circuitBreaker) {
        return from(callable, Failsafe.with(retryPolicy).with(circuitBreaker).with(executor));
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(Callable<Promise<D, F>> callable,
            AsyncFailsafe<Object> failSafe) {
        CompletableFuture<D> thisFuture = failSafe.future(() -> {
            Promise<D, F> promise = callable.call();
            CompletableFuture<D> future = new CompletableFuture<>();
            promise.done(future::complete).fail(future::completeExceptionally);
            return future;
        });
        return new FailSafePromise<>(thisFuture);
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(
            Function<ExecutionContext, Promise<D, F>> callable, ScheduledExecutorService executor,
            RetryPolicy retryPolicy) {
        return from(callable, Failsafe.with(retryPolicy).with(executor));
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(
            Function<ExecutionContext, Promise<D, F>> callable, ScheduledExecutorService executor,
            RetryPolicy retryPolicy, CircuitBreaker circuitBreaker) {
        return from(callable, Failsafe.with(retryPolicy).with(circuitBreaker).with(executor));
    }

    public static <D, F extends Throwable> FailSafePromise<D, F> from(
            Function<ExecutionContext, Promise<D, F>> callable, AsyncFailsafe<Object> failSafe) {
        CompletableFuture<D> thisFuture = failSafe.future(executionContext -> {
            Promise<D, F> promise = callable.apply(executionContext);
            CompletableFuture<D> future = new CompletableFuture<>();
            promise.done(future::complete).fail(future::completeExceptionally);
            return future;
        });
        return new FailSafePromise<>(thisFuture);
    }
}