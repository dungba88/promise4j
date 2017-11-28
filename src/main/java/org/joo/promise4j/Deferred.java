package org.joo.promise4j;

/**
 * Represents a Deferred object
 * 
 * @author griever
 *
 * @param <D>
 *            the response type when the deferred is resolved
 * @param <F>
 *            the exception type when the deferred is rejected
 */
public interface Deferred<D, F extends Throwable> {

    public Deferred<D, F> resolve(final D result);

    public Deferred<D, F> reject(final F failedCause);

    public Promise<D, F> promise();
}
