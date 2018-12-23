package org.joo.promise4j;

/**
 * Represents a filter for fulfilled promises
 * 
 * @author griever
 *
 * @param <D> the response type of the original promise
 * @param <D_OUT> the response type after filtering
 */
public interface FilteredDoneCallback<D, D_OUT> {

    /**
     * A callback to be called when the promise is fulfilled. Implementations can
     * transform the result to their desired object
     * 
     * @param result the result of the promise
     * @return the filtered result
     */
    public D_OUT onDone(final D result);
}
