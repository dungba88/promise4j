package org.joo.promise4j.impl;

import java.util.function.Predicate;

import org.joo.promise4j.FilteredDoneCallback;
import org.joo.promise4j.FilteredFailureCallback;
import org.joo.promise4j.PipeAlwaysCallback;
import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;

public abstract class AbstractPromise<D, F extends Throwable> implements Promise<D, F> {
    
    @Override
    public Promise<D, F> when(Predicate<D> predicate, PipeDoneCallback<D, D, F> callback) {
        return new ConditionalPipedPromise<>(this, callback, predicate);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> then(
            final PipeAlwaysCallback<D, D_OUT, F, F_OUT> alwaysCallback) {
        return new PipedAlwaysPromise<>(this, alwaysCallback);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeDone(
            final PipeDoneCallback<D, D_OUT, F_OUT> doneCallback) {
        return new PipedPromise<>(this, doneCallback, null);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeFail(
            final PipeFailureCallback<F, D_OUT, F_OUT> failCallback) {
        return new PipedPromise<>(this, null, failCallback);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F> filterDone(
            final FilteredDoneCallback<D, D_OUT> doneCallback) {
        return new FilteredPromise<>(this, doneCallback, null);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterFail(
            final FilteredFailureCallback<F, F_OUT> failCallback) {
        return new FilteredPromise<>(this, null, failCallback);
    }
}
