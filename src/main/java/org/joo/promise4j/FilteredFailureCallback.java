package org.joo.promise4j;

/**
 * Represents a filter for rejected promise
 * 
 * @author griever
 *
 * @param <F>
 *            the exception type of the original promise
 * @param <F_OUT>
 *            the exception type after filtering
 */
public interface FilteredFailureCallback<F extends Throwable, F_OUT extends Throwable> {

    /**
     * A callback to be called when the promise is rejected
     * 
     * @param cause
     *            the original cause of the failure
     * @return the filtered failure cause
     */
    public F_OUT onFail(final F cause);
}
