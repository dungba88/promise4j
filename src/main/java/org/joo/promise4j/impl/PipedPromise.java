package org.joo.promise4j.impl;

import org.joo.promise4j.PipeDoneCallback;
import org.joo.promise4j.PipeFailureCallback;
import org.joo.promise4j.Promise;

public class PipedPromise<D, F extends Throwable, D_OUT, F_OUT extends Throwable>
        extends CompletableDeferredObject<D_OUT, F_OUT> {

    @SuppressWarnings("unchecked")
    public PipedPromise(final Promise<D, F> promise, final PipeDoneCallback<D, D_OUT, F_OUT> doneCallback,
            final PipeFailureCallback<F, D_OUT, F_OUT> failCallback) {
        promise.done(response -> {
            if (doneCallback != null) {
                try {
                    doneCallback.onDone(response).forward(this);
                } catch (Throwable ex) {
                    reject((F_OUT) ex);
                }
            } else
                resolve((D_OUT) response);
        }).fail(ex -> {
            if (failCallback != null) {
                try {
                    failCallback.onFail(ex).forward(this);
                } catch (Throwable cause) {
                    reject((F_OUT) cause);
                }
            } else
                reject((F_OUT) ex);
        });
    }
}