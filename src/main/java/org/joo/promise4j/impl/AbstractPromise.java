package org.joo.promise4j.impl;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.FilteredDoneCallback;
import org.joo.promise4j.FilteredFailureCallback;
import org.joo.promise4j.PipeAlwaysCallback;
import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public abstract class AbstractPromise<D, F extends Throwable> implements Promise<D, F> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPromise.class);

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



    protected void complete(final DoneCallback<D> callback, D response) {
        try {
            callback.onDone(response);
        } catch (Throwable ex) {
            LOGGER.error("Exception caught while handling done", ex);
            throw ex;
        }
    }

    @SuppressWarnings("unchecked")
    protected D complete(final FailCallback<F> callback, Throwable ex) {
        try {
            callback.onFail((F) ex);
            return null;
        } catch (Throwable failure) {
            LOGGER.error("Exception caught while handling fail", failure);
            throw failure;
        }
    }

    @SuppressWarnings("unchecked")
    protected void complete(AlwaysCallback<D, F> callback, D result, Throwable cause) {
        try {
            callback.onAlways(cause != null ? DeferredStatus.REJECTED : DeferredStatus.RESOLVED, result, (F) cause);
        } catch (Throwable failure) {
            LOGGER.error("Exception caught while handling always", failure);
            throw failure;
        }
    }
}
