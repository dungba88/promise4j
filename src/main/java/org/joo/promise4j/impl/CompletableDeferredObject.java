package org.joo.promise4j.impl;

import java.util.concurrent.CompletableFuture;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;

public class CompletableDeferredObject<D, F extends Throwable> extends AbstractPromise<D, F> implements Deferred<D, F> {
    
    private CompletableFuture<D> future;
    
    public CompletableDeferredObject() {
        this.future = new CompletableFuture<>();
    }
    
    @Override
    public Promise<D, F> done(final DoneCallback<D> callback) {
        future.thenAccept((response) -> callback.onDone(response));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Promise<D, F> fail(final FailCallback<F> callback) {
        future.exceptionally(ex -> {
            callback.onFail((F) ex);
            return null;
        });
        return this;
    }

    @Override
    public Deferred<D, F> resolve(final D result) {
        future.complete(result);
        return this;
    }

    @Override
    public Deferred<D, F> reject(final F failedCause) {
        future.completeExceptionally(failedCause);
        return this;
    }

    @Override
    public Promise<D, F> promise() {
        return this;
    }
}