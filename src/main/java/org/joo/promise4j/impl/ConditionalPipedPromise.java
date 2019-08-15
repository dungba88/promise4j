package org.joo.promise4j.impl;

import java.util.Objects;
import java.util.function.Predicate;

import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.Promise;

public class ConditionalPipedPromise<D, F extends Throwable> extends CompletableDeferredObject<D, F> {

    @SuppressWarnings("unchecked")
    public ConditionalPipedPromise(final Promise<D, F> promise, final PipeDoneCallback<D, D, F> callback,
            final Predicate<D> predicate) {
        Objects.requireNonNull(callback);
        Objects.requireNonNull(predicate);
        promise.done(response -> {
            try {
                if (predicate.test(response)) {
                    callback.onDone(response).forward(this);
                } else {
                    resolve((D) response);
                }
            } catch (Throwable ex) {
                reject((F) ex);
            }
        }).fail(this::reject);
    }
}