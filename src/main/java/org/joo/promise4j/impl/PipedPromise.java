package org.joo.promise4j.impl;

import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;

public class PipedPromise<D, F extends Throwable, D_OUT, F_OUT extends Throwable> extends CompletableDeferredObject<D_OUT, F_OUT> {
    
    @SuppressWarnings("unchecked")
    public PipedPromise(Promise<D, F> promise, PipeDoneCallback<D, D_OUT, F_OUT> doneCallback, PipeFailureCallback<F, D_OUT, F_OUT> failCallback) {
        promise.done(response -> {
            if (doneCallback != null) pipe(doneCallback.onDone(response));
            else resolve((D_OUT) response);
        }).fail(ex -> {
            if (failCallback != null) pipe(failCallback.onFail(ex));
            else reject((F_OUT) ex);
        });
    }

    private void pipe(Promise<D_OUT, F_OUT> promise) {
        promise.done(response -> {
            resolve(response);
        }).fail(ex -> {
            reject(ex);
        });
    }
}