package org.joo.promise4j.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.FilteredDoneCallback;
import org.joo.promise4j.FilteredFailureCallback;
import org.joo.promise4j.PipeAlwaysCallback;
import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;

public class SimpleDeferredObject<D, F extends Throwable> implements Deferred<D, F>, Promise<D, F> {

    private DeferredStatus status;

    private AlwaysCallback<D, F> alwaysCallback;

    private DoneCallback<D> doneCallback;

    private FailCallback<F> failCallback;

    public SimpleDeferredObject(final DoneCallback<D> doneCallback, final FailCallback<F> failCallback) {
        this(doneCallback, failCallback, null);
    }

    public SimpleDeferredObject(final DoneCallback<D> doneCallback, final FailCallback<F> failCallback,
            final AlwaysCallback<D, F> alwaysCallback) {
        this.alwaysCallback = alwaysCallback;
        this.doneCallback = doneCallback;
        this.failCallback = failCallback;
    }

    @Override
    public Deferred<D, F> resolve(final D resolve) {
        if (!isPending())
            return this;

        this.status = DeferredStatus.RESOLVED;
        triggerDone(doneCallback, resolve);
        triggerAlways(alwaysCallback, resolve, null);
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F reject) {
        if (!isPending())
            return this;

        this.status = DeferredStatus.REJECTED;
        triggerFail(failCallback, reject);
        triggerAlways(alwaysCallback, null, reject);
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

    public boolean isPending() {
        return status == null;
    }

    @Override
    public Promise<D, F> promise() {
        return this;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public Promise<D, F> always(AlwaysCallback<D, F> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    private void triggerAlways(AlwaysCallback<D, F> callback, D resolve, F reject) {
        if (callback != null)
            callback.onAlways(status, resolve, reject);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeDone(
            final PipeDoneCallback<D, D_OUT, F_OUT> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeFail(
            final PipeFailureCallback<F, D_OUT, F_OUT> failCallback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F> filterDone(
            final FilteredDoneCallback<D, D_OUT> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterFail(
            final FilteredFailureCallback<F, F_OUT> failCallback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public D get() throws PromiseException {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public Promise<D, F> when(Predicate<D> predicate, PipeDoneCallback<D, D, F> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> then(
            PipeAlwaysCallback<D, D_OUT, F, F_OUT> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public Deferred<D, F> withTimeout(long timeout, TimeUnit unit, Supplier<F> exceptionSupplier) {
        return this;
    }

    @Override
    public Promise<D, F> timeoutAfter(long duration, TimeUnit unit, Supplier<F> exceptionSupplier) {
        withTimeout(duration, unit, exceptionSupplier);
        return this;
    }

    @Override
    public DeferredStatus getStatus() {
        return status;
    }
}