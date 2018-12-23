package org.joo.promise4j;

/**
 * Represents a completed callback
 * 
 * @author griever
 *
 * @param <D> the response type when the promise is fulfilled
 * @param <F> the exception type when the promise is rejected
 */
public interface AlwaysCallback<D, F extends Throwable> {

    /**
     * A callback to be called when the deferred/promise completes
     * 
     * @param status the status of the promise
     * @param result the result of the promise if any
     * @param cause  the cause of the failure if any
     */
    public void onAlways(final DeferredStatus status, final D result, final F cause);
}
