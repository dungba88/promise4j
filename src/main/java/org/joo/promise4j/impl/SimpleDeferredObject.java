package org.joo.promise4j.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;

public class SimpleDeferredObject<D, F extends Throwable> implements Deferred<D, F>, Promise<D, F> {

    private DeferredStatus status;

    private DoneCallback<D> doneCallback;

    private FailCallback<F> failCallback;

    public SimpleDeferredObject(DoneCallback<D> doneCallback, FailCallback<F> failCallback) {
        this.doneCallback = doneCallback;
        this.failCallback = failCallback;
    }

    @Override
    public Deferred<D, F> resolve(final D resolve) {
        if (!isPending())
            throw new IllegalStateException("Deferred is already resolved or rejected");

        this.status = DeferredStatus.RESOLVED;
        triggerDone(doneCallback, resolve);
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F reject) {
        if (!isPending())
            throw new IllegalStateException("Deferred is already resolved or rejected");
        this.status = DeferredStatus.REJECTED;
        triggerFail(failCallback, reject);
        return this;
    }

    @Override
    public Promise<D, F> done(DoneCallback<D> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    @Override
    public Promise<D, F> fail(FailCallback<F> callback) {
        throw new UnsupportedOperationException("Callback cannot be deferred in non-deferred mode");
    }

    private void triggerDone(DoneCallback<D> callback, D resolve) {
        if (callback != null) {
            callback.onDone(resolve);
        }
    }

    private void triggerFail(FailCallback<F> callback, F reject) {
        if (callback != null) {
            callback.onFail(reject);
        }
    }

    public Promise<D, F> promise() {
        return this;
    }

    public boolean isPending() {
        return status == null;
    }
}