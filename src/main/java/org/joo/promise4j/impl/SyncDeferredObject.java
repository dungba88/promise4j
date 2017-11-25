package org.joo.promise4j.impl;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;

public class SyncDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {

    private D result;

    private F failedCause;

    private volatile DeferredStatus status;

    private DoneCallback<D> doneCallback;

    private FailCallback<F> failCallback;

    @Override
    public Deferred<D, F> resolve(final D resolve) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Deferred is already resolved or rejected");

            this.status = DeferredStatus.RESOLVED;
            this.result = resolve;
            triggerDone(doneCallback, resolve);
        }
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F reject) {
        synchronized (this) {
            if (!isPending())
                throw new IllegalStateException("Deferred is already resolved or rejected");
            this.status = DeferredStatus.REJECTED;
            this.failedCause = reject;
            triggerFail(failCallback, reject);
        }
        return this;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        synchronized (this) {
            if (isResolved()) {
                triggerDone(callback, result);
            } else {
                doneCallback = callback;
            }
        }
        return this;
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        synchronized (this) {
            if (isRejected()) {
                triggerFail(callback, failedCause);
            } else {
                failCallback = callback;
            }
        }
        return this;
    }

    private void triggerDone(final DoneCallback<D> callback, final D resolve) {
        if (callback != null) {
            callback.onDone(resolve);
        }
    }

    private void triggerFail(final FailCallback<F> callback, final F reject) {
        if (callback != null) {
            callback.onFail(reject);
        }
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
}