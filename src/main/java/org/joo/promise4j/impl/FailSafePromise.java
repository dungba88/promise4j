package org.joo.promise4j.impl;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.joo.promise4j.Promise;

import net.jodah.failsafe.AsyncFailsafe;
import net.jodah.failsafe.ExecutionContext;

public class FailSafePromise<D, F extends Throwable> extends CompletableDeferredObject<D, F> {

	private FailSafePromise(final CompletableFuture<D> future) {
		super(future);
	}

	public static <D, F extends Throwable> FailSafePromise<D, F> from(Supplier<D> supplier,
			AsyncFailsafe<Object> failSafe) {
		CompletableFuture<D> thisFuture = failSafe.future(executionContext -> {
			CompletableFuture<D> future = CompletableFuture.supplyAsync(supplier);
			return future;
		});
		return new FailSafePromise<>(thisFuture);
	}

	public static <D, F extends Throwable> FailSafePromise<D, F> from(Function<ExecutionContext, D> callable,
			AsyncFailsafe<Object> failSafe) {
		CompletableFuture<D> thisFuture = failSafe.future(executionContext -> {
			CompletableFuture<D> future = CompletableFuture.supplyAsync(() -> {
				return callable.apply(executionContext);
			});
			return future;
		});
		return new FailSafePromise<>(thisFuture);
	}

	public static <D, F extends Throwable> FailSafePromise<D, F> fromPromise(Supplier<Promise<D, F>> supplier,
			AsyncFailsafe<Object> failSafe) {
		CompletableFuture<D> thisFuture = failSafe.future(() -> {
			Promise<D, F> promise = supplier.get();
			return promiseToFuture(promise);
		});
		return new FailSafePromise<>(thisFuture);
	}

	public static <D, F extends Throwable> FailSafePromise<D, F> fromPromise(
			Function<ExecutionContext, Promise<D, F>> callable, AsyncFailsafe<Object> failSafe) {
		CompletableFuture<D> thisFuture = failSafe.future(executionContext -> {
			Promise<D, F> promise = callable.apply(executionContext);
			return promiseToFuture(promise);
		});
		return new FailSafePromise<>(thisFuture);
	}

	private static <D, F extends Throwable> CompletableFuture<D> promiseToFuture(Promise<D, F> promise) {
		CompletableFuture<D> future = new CompletableFuture<>();
		promise.done(future::complete).fail(future::completeExceptionally);
		return future;
	}
}