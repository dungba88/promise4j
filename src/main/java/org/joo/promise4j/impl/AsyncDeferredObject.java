package org.joo.promise4j.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.util.ThreadHints;
import org.joo.promise4j.util.TimeoutScheduler;

public class AsyncDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {

	private static final long SPIN_FOR_TIMEOUT_THRESHOLDS = 1000L;

	private D result;

	private F failedCause;

	private volatile AlwaysCallback<D, F> alwaysCallback;

	private volatile DoneCallback<D> doneCallback;

	private volatile FailCallback<F> failureCallback;

	private volatile DeferredStatus status;

	private AtomicBoolean done;

	private AtomicBoolean alert;

	private AtomicBoolean alwaysAlert;

	public AsyncDeferredObject() {
		this.done = new AtomicBoolean(false);
		this.alert = new AtomicBoolean(false);
		this.alwaysAlert = new AtomicBoolean(false);
	}

	@Override
	public Deferred<D, F> resolve(final D result) {
		if (!done.compareAndSet(false, true))
			return this;
		this.result = result;
		this.status = DeferredStatus.RESOLVED;
		this.onComplete(result);
		return this;
	}

	@Override
	public Deferred<D, F> reject(final F failedCause) {
		if (!done.compareAndSet(false, true))
			return this;
		this.failedCause = failedCause;
		this.status = DeferredStatus.REJECTED;
		this.onFail(failedCause);
		return this;
	}

	private void onComplete(final D result) {
		if (doneCallback != null && alert.compareAndSet(false, true))
			doneCallback.onDone(result);
		if (alwaysCallback != null && alwaysAlert.compareAndSet(false, true))
			alwaysCallback.onAlways(DeferredStatus.RESOLVED, result, null);
	}

	private void onFail(final F failedCause) {
		if (failureCallback != null && alert.compareAndSet(false, true))
			failureCallback.onFail(failedCause);
		if (alwaysCallback != null && alwaysAlert.compareAndSet(false, true))
			alwaysCallback.onAlways(DeferredStatus.REJECTED, null, failedCause);
	}

	@Override
	public Promise<D, F> promise() {
		return this;
	}

	@Override
	public Promise<D, F> always(AlwaysCallback<D, F> callback) {
		alwaysCallback = callback;
		if (status != null && alwaysAlert.compareAndSet(false, true))
			callback.onAlways(status, result, failedCause);
		return this;
	}

	@Override
	public Promise<D, F> done(final DoneCallback<D> callback) {
		doneCallback = callback;
		if (status == DeferredStatus.RESOLVED && alert.compareAndSet(false, true))
			callback.onDone(result);
		return this;
	}

	@Override
	public Promise<D, F> fail(final FailCallback<F> callback) {
		this.failureCallback = callback;
		if (status == DeferredStatus.REJECTED && alert.compareAndSet(false, true))
			callback.onFail(failedCause);
		return this;
	}

	@Override
	public D get() throws PromiseException, InterruptedException {
		while (true) {
			if (Thread.interrupted())
				throw new InterruptedException();
			if (status == DeferredStatus.RESOLVED)
				return result;
			if (status == DeferredStatus.REJECTED)
				throw new PromiseException(failedCause);
			LockSupport.parkNanos(0L);
		}
	}

	@Override
	public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
		long waitTime = unit.toNanos(timeout);
		long start = System.nanoTime();
		while (true) {
			if (Thread.interrupted())
				throw new InterruptedException();
			if (status == DeferredStatus.RESOLVED)
				return result;
			if (status == DeferredStatus.REJECTED)
				throw new PromiseException(failedCause);
			long remainingTime = waitTime - (System.nanoTime() - start);
			if (remainingTime <= 0L)
				throw new TimeoutException();
			if (remainingTime >= SPIN_FOR_TIMEOUT_THRESHOLDS)
				LockSupport.parkNanos(0L);
			else
				ThreadHints.onSpinWait();
		}
	}

	@Override
	public Deferred<D, F> withTimeout(long timeout, TimeUnit unit, Supplier<F> exceptionSupplier) {
		TimeoutScheduler.delay(() -> {
			if (status == null)
				reject(exceptionSupplier.get());
		}, timeout, unit);
		return this;
	}

	@Override
	public DeferredStatus getStatus() {
		return status;
	}
}