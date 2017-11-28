package org.joo.promise4j;

/**
 * Represents a Promise
 * 
 * @author griever
 *
 * @param <D>
 *            the response type when promise is fulfilled
 * @param <F>
 *            the exception type when promise is rejected
 */
public interface Promise<D, F extends Throwable> {

    /**
     * Register a callback when promise is fulfilled. This method will return the
     * same promise.
     */
    public Promise<D, F> done(DoneCallback<D> callback);

    /**
     * Register a callback when promise is rejected. This method will return the
     * same promise.
     */
    public Promise<D, F> fail(FailCallback<F> callback);

    /**
     * Register a callback when promise is completed, regardless whether it is
     * fulfilled or rejected. This method will return the same promise.
     */
    public Promise<D, F> always(AlwaysCallback<D, F> callback);

    /**
     * Register a piped callback when the previous promise is fulfilled. This method
     * will return a new Promise object.
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeDone(PipeDoneCallback<D, D_OUT, F_OUT> callback);

    /**
     * Register a piped callback when the previous promise is rejected. This method
     * will return a new Promise object.
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeFail(
            PipeFailureCallback<F, D_OUT, F_OUT> failCallback);

    /**
     * Register a filtered callback when the previous promise is fulfilled. This method
     * will return a new Promise object.
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterDone(FilteredDoneCallback<D, D_OUT> callback);

    /**
     * Register a filtered callback when the previous promise is rejected. This method
     * will return a new Promise object.
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterFail(
            FilteredFailureCallback<F, F_OUT> failCallback);
}
