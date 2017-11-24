package org.joo.promise4j.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;

public class AsyncDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {

    private D result;

    private F failedCause;

    private volatile DoneCallback<D> doneCallback;

    private volatile FailCallback<F> failureCallback;

    private volatile DeferredStatus status;

    private AtomicBoolean done;

    private AtomicBoolean alert;

    public AsyncDeferredObject() {
        this.done = new AtomicBoolean(false);
        this.alert = new AtomicBoolean(false);
    }

    @Override
    public Deferred<D, F> resolve(final D result) {
        if (!done.compareAndSet(false, true))
            throw new IllegalStateException("Deferred is already resolved or rejected");
        this.result = result;
        this.status = DeferredStatus.RESOLVED;
        this.onComplete(result);
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F failedCause) {
        if (!done.compareAndSet(false, true))
            throw new IllegalStateException("Deferred is already resolved or rejected");
        this.failedCause = failedCause;
        this.status = DeferredStatus.REJECTED;
        this.onFail(failedCause);
        return this;
    }

    private void onComplete(D result) {
        if (doneCallback != null && alert.compareAndSet(false, true)) {
            doneCallback.onDone(result);
        }
    }

    private void onFail(F failedCause) {
        if (failureCallback != null && alert.compareAndSet(false, true)) {
            failureCallback.onFail(failedCause);
        }
    }

    @Override
    public Promise<D, F> promise() {
        return this;
    }

    @Override
    public Promise<D, F> done(DoneCallback<D> callback) {
        doneCallback = callback;
        if (status == DeferredStatus.RESOLVED && alert.compareAndSet(false, true))
            callback.onDone(result);
        return this;
    }

    @Override
    public Promise<D, F> fail(FailCallback<F> callback) {
        this.failureCallback = callback;
        if (status == DeferredStatus.REJECTED && alert.compareAndSet(false, true))
            callback.onFail(failedCause);
        return this;
    }
}