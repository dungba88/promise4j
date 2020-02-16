package org.joo.promise4j.impl;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class SimpleFailurePromise<D, F extends Throwable> extends AbstractPromise<D, F> {

    private F failedCause;

    public SimpleFailurePromise(final F failedCause) {
        this.failedCause = failedCause;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        return this;
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        complete(callback, failedCause);
        return this;
    }

    @Override
    public Promise<D, F> always(AlwaysCallback<D, F> callback) {
        complete(callback, null, failedCause);
        return this;
    }

    @Override
    public D get() throws PromiseException {
        throw new PromiseException(failedCause);
    }

    @Override
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
        throw new PromiseException(failedCause);
    }

    @Override
    public DeferredStatus getStatus() {
        return DeferredStatus.REJECTED;
    }

    @Override
    public Promise<D, F> timeoutAfter(long duration, TimeUnit unit, Supplier<F> exceptionSupplier) {
        // do nothing
        return this;
    }
}
