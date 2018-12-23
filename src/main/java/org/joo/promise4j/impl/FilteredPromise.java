package org.joo.promise4j.impl;

import org.joo.promise4j.FilteredDoneCallback;
import org.joo.promise4j.FilteredFailureCallback;
import org.joo.promise4j.Promise;

public class FilteredPromise<D, F extends Throwable, D_OUT, F_OUT extends Throwable>
        extends CompletableDeferredObject<D_OUT, F_OUT> {

    @SuppressWarnings("unchecked")
    public FilteredPromise(final Promise<D, F> promise, final FilteredDoneCallback<D, D_OUT> doneFilter,
            final FilteredFailureCallback<F, F_OUT> failFilter) {
        promise.done(response -> {
            if (doneFilter != null) {
                try {
                    resolve(doneFilter.onDone(response));
                } catch (Throwable ex) {
                    reject((F_OUT) ex);
                }
            } else
                resolve((D_OUT) response);
        }).fail(ex -> {
            if (failFilter != null) {
                try {
                    reject(failFilter.onFail(ex));
                } catch (Throwable cause) {
                    reject((F_OUT) cause);
                }
            } else
                reject((F_OUT) ex);
        });
    }
}
