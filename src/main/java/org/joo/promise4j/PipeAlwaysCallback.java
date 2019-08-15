package org.joo.promise4j;

/**
 * Represents a pipe for fulfilled promise. Pipe is similar to filter, except
 * that they returns a {@link org.joo.promise4j.Promise} instead of the result.
 * 
 * @author griever
 *
 * @param <D> the response type when the promise is fulfilled
 * @param <D_OUT> the response type after piping
 * @param <F_OUT> the exception type after piping
 */
public interface PipeAlwaysCallback<D, D_OUT, F extends Throwable, F_OUT extends Throwable> {

    /**
     * A callback to be called when the promise is fulfilled.
     * 
     * @param result the result of the promise
     * @return the new promise
     */
    public Promise<D_OUT, F_OUT> onAlways(final DeferredStatus status, final D result, final F cause);
}
