package org.joo.promise4j.impl;

import java.util.Arrays;
import java.util.Collection;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

public class SequentialPromise<D, F extends Throwable> extends CompletableDeferredObject<D, F> {

    private SequentialPromise() {

    }

    @SafeVarargs
    public static final <D, F extends Throwable> Promise<D, F> of(Promise<D, F>... promises) {
        Promise<D, F> result = null;
        for (Promise<D, F> promise : promises) {
            if (result == null)
                result = promise;
            else
                result = result.pipeDone(r -> promise);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> Promise<D, F> of(Deferred<D, F>... deferreds) {
        Promise<D, F>[] promises = Arrays.stream(deferreds) //
                                         .map(deferred -> deferred.promise()) //
                                         .toArray(size -> new Promise[size]);
        return of(promises);
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> Promise<D, F> of(Collection<Promise<D, F>> promises) {
        return of(promises.toArray(new Promise[0]));
    }
}