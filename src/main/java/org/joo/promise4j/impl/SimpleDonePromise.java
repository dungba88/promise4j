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

public class SimpleDonePromise<D, F extends Throwable> extends AbstractPromise<D, F> {

    private D result;

    public SimpleDonePromise(final D result) {
        this.result = result;
    }

    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        complete(callback, result);
        return this;
    }

    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        return this;
    }

    @Override
    public Promise<D, F> always(AlwaysCallback<D, F> callback) {
        complete(callback, result, null);
        return this;
    }

    @Override
    public D get() {
        return result;
    }

    @Override
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
        return get();
    }

    @Override
    public DeferredStatus getStatus() {
        return DeferredStatus.RESOLVED;
    }

    @Override
    public Promise<D, F> timeoutAfter(long duration, TimeUnit unit, Supplier<F> exceptionSupplier) {
        // do nothing
        return this;
    }
}
