package org.joo.promise4j.impl;

import java.util.Collection;
import java.util.function.Supplier;

import org.joo.promise4j.Promise;

public class SequentialPromise<D, F extends Throwable> extends CompletableDeferredObject<D, F> {

    private SequentialPromise() {

    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static final <D, F extends Throwable> Promise<D, F> of(Supplier<Promise<?, ?>>... suppliers) {
        Promise<?, ?> result = null;
        for (Supplier<Promise<?, ?>> supplier : suppliers) {
            if (result == null)
                result = supplier.get();
            else
                result = result.pipeDone(r -> supplier.get());
        }
        return (Promise<D, F>) result;
    }

    @SuppressWarnings("unchecked")
    public static final <D, F extends Throwable> Promise<D, F> of(Collection<Supplier<Promise<D, F>>> promises) {
        return of(promises.toArray(new Supplier[0]));
    }
}