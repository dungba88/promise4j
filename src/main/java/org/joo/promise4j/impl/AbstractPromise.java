package org.joo.promise4j.impl;

import org.joo.promise4j.FilteredDoneCallback;
import org.joo.promise4j.FilteredFailureCallback;
import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;

public abstract class AbstractPromise<D, F extends Throwable> implements Promise<D, F> {

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
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterDone(
            final FilteredDoneCallback<D, D_OUT> doneCallback) {
        return new FilteredPromise<>(this, doneCallback, null);
    }

    @Override
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterFail(
            final FilteredFailureCallback<F, F_OUT> failCallback) {
        return new FilteredPromise<>(this, null, failCallback);
    }
}
