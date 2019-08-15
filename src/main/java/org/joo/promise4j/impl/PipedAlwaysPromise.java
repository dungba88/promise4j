package org.joo.promise4j.impl;

import org.joo.promise4j.PipeAlwaysCallback;
import org.joo.promise4j.Promise;

public class PipedAlwaysPromise<D, F extends Throwable, D_OUT, F_OUT extends Throwable>
        extends CompletableDeferredObject<D_OUT, F_OUT> {

    @SuppressWarnings("unchecked")
    public PipedAlwaysPromise(final Promise<D, F> promise,
            final PipeAlwaysCallback<D, D_OUT, F, F_OUT> alwaysCallback) {
        promise.always((status, response, ex) -> {
            try {
                alwaysCallback.onAlways(status, response, ex).forward(this);
            } catch (Throwable ex1) {
                reject((F_OUT) ex1);
            }
        });
    }
}