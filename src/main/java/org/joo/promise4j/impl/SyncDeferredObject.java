package org.joo.promise4j.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.util.TimeoutScheduler;

public class SyncDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {

    private D result;

    private F failedCause;

    private volatile DeferredStatus status;

    private AlwaysCallback<D, F> alwaysCallback;

    private DoneCallback<D> doneCallback;

    private FailCallback<F> failCallback;

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    public Deferred<D, F> resolve(final D resolve) {
        synchronized (this) {
            if (!isPending())
                return this;

            this.status = DeferredStatus.RESOLVED;
            this.result = resolve;
            triggerDone(doneCallback, resolve);
            triggerAlways(alwaysCallback, resolve, null);
        }
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F reject) {
        synchronized (this) {
            if (!isPending())
                return this;

            this.status = DeferredStatus.REJECTED;
            this.failedCause = reject;
            triggerFail(failCallback, reject);
            triggerAlways(alwaysCallback, null, reject);
        }
        return this;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        synchronized (this) {
            if (isResolved())
                triggerDone(callback, result);
            else
                doneCallback = callback;
        }
        return this;
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        synchronized (this) {
            if (isRejected())
                triggerFail(callback, failedCause);
            else
                failCallback = callback;
        }
        return this;
    }

    @Override
    public Promise<D, F> always(AlwaysCallback<D, F> callback) {
        synchronized (this) {
            if (isRejected())
                triggerAlways(callback, null, failedCause);
            else if (isResolved())
                triggerAlways(callback, result, null);
            else
                alwaysCallback = callback;
        }
        return this;
    }

    private void triggerDone(final DoneCallback<D> callback, final D resolve) {
        if (callback != null)
            callback.onDone(resolve);
    }

    private void triggerFail(final FailCallback<F> callback, final F reject) {
        if (callback != null)
            callback.onFail(reject);
    }

    private void triggerAlways(final AlwaysCallback<D, F> callback, final D resolve, final F reject) {
        if (callback != null)
            callback.onAlways(status, resolve, reject);

        latch.countDown();
    }

    @Override
    public Promise<D, F> promise() {
        return this;
    }

    public boolean isPending() {
        return status == null;
    }

    public boolean isResolved() {
        return status == DeferredStatus.RESOLVED;
    }

    public boolean isRejected() {
        return status == DeferredStatus.REJECTED;
    }

    @Override
    public D get() throws PromiseException, InterruptedException {
        latch.await();
        if (status == DeferredStatus.REJECTED)
            throw new PromiseException(failedCause);
        return result;
    }

    @Override
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
        latch.await(timeout, unit);
        DeferredStatus theStatus = status;
        if (theStatus == DeferredStatus.REJECTED)
            throw new PromiseException(failedCause);
        if (theStatus == DeferredStatus.RESOLVED)
            return result;
        throw new TimeoutException();
    }

    @Override
    public Deferred<D, F> withTimeout(long timeout, TimeUnit unit, Supplier<F> exceptionSupplier) {
        TimeoutScheduler.delay(() -> {
            if (isPending())
                reject(exceptionSupplier.get());
        }, timeout, unit);
        return this;
    }

    @Override
    public DeferredStatus getStatus() {
        return status;
    }

    @Override
    public Promise<D, F> timeoutAfter(long duration, TimeUnit unit, Supplier<F> exceptionSupplier) {
        withTimeout(duration, unit, exceptionSupplier);
        return this;
    }
}