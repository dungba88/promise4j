package org.joo.promise4j;

public interface Deferred<D, F extends Throwable> {

    public Deferred<D, F> resolve(final D result);

    public Deferred<D, F> reject(final F failedCause);

    public Promise<D, F> promise();
}
