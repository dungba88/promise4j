package org.joo.promise4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.joo.promise4j.impl.JoinedPromise;
import org.joo.promise4j.impl.JoinedResults;
import org.joo.promise4j.impl.SequentialPromise;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;

/**
 * Represents a Promise
 * 
 * @author griever
 *
 * @param <D> the response type when promise is fulfilled
 * @param <F> the exception type when promise is rejected
 */
public interface Promise<D, F extends Throwable> {

    public DeferredStatus getStatus();

    /**
     * Register a callback when promise is fulfilled. This method will return the
     * same promise.
     * 
     * @param callback the callback
     * @return the current promise
     */
    public Promise<D, F> done(DoneCallback<D> callback);

    /**
     * Register a callback when promise is rejected. This method will return the
     * same promise.
     * 
     * @param callback the callback
     * @return the current promise
     */
    public Promise<D, F> fail(FailCallback<F> callback);

    /**
     * Register a callback when promise is completed, regardless whether it is
     * fulfilled or rejected. This method will return the same promise.
     * 
     * @param callback the callback
     * @return the current promise
     */
    public Promise<D, F> always(AlwaysCallback<D, F> callback);

    /**
     * Forward the result of this promise to a Deferred object.
     * 
     * @param deferred the deferred object to be forward
     * @return the current promise
     */
    public default Promise<D, F> forward(Deferred<D, F> deferred) {
        if (deferred == null)
            return this;
        return done(deferred::resolve).fail(deferred::reject);
    }

    /**
     * Wait and get for the result. If the promise is rejected, the a exception will
     * be thrown
     * 
     * @return the result
     * @throws PromiseException     if the promise is rejected
     * @throws InterruptedException if the thread is interrupted while waiting for
     *                              the result
     */
    public D get() throws PromiseException, InterruptedException;

    /**
     * Wait and get for the result for a specified timeout. If the promise is
     * rejected, the a PromiseException will be raised. If the promise has not been
     * completed after timeout, a TimeoutException will be raised.
     * 
     * @param timeout the timeout duration
     * @param unit    the time unit for the timeout duration
     * 
     * @return the result
     * @throws PromiseException     if the promise is rejected
     * @throws InterruptedException if the thread is interrupted while waiting for
     *                              the result
     * @throws TimeoutException     if the promise is not completed after timeout
     *                              period
     */
    public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException;

    /**
     * Register a piped callback when the previous promise is fulfilled. This method
     * will return a new Promise object.
     * 
     * @param callback the callback
     * @return the new promise
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeDone(PipeDoneCallback<D, D_OUT, F_OUT> callback);

    /**
     * Register a piped callback when the previous promise is rejected. This method
     * will return a new Promise object.
     * 
     * @param callback the callback
     * @return the new promise
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> pipeFail(
            PipeFailureCallback<F, D_OUT, F_OUT> callback);

    /**
     * Register a filtered callback when the previous promise is fulfilled. This
     * method will return a new Promise object.
     * 
     * @param callback the callback
     * @return the new promise
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterDone(FilteredDoneCallback<D, D_OUT> callback);

    /**
     * Register a filtered callback when the previous promise is rejected. This
     * method will return a new Promise object.
     * 
     * @param callback the callback
     * @return the new promise
     */
    public <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> filterFail(
            FilteredFailureCallback<F, F_OUT> callback);

    /**
     * Alias for {@link #filterDone(FilteredDoneCallback)}
     * 
     * @param callback the callback
     * @return the filtered promise
     */
    public default <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> map(FilteredDoneCallback<D, D_OUT> callback) {
        return filterDone(callback);
    }

    /**
     * Alias for {@link #done(FilteredDoneCallback)}
     * 
     * @param callback the callback
     * @return the filtered promise
     */
    public default Promise<D, F> then(DoneCallback<D> callback) {
        return done(callback);
    }

    /**
     * Alias for {@link #pipeDone(FilteredDoneCallback)}
     * 
     * @param callback the callback
     * @return the filtered promise
     */
    public default <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> then(
            PipeDoneCallback<D, D_OUT, F_OUT> callback) {
        return pipeDone(callback);
    }

    public static <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> ofCause(F_OUT cause) {
        return new SimpleFailurePromise<>(cause);
    }

    public static <D_OUT, F_OUT extends Throwable> Promise<D_OUT, F_OUT> of(D_OUT result) {
        return new SimpleDonePromise<>(result);
    }

    @SafeVarargs
    public static <D, F extends Throwable> Promise<JoinedResults<D>, F> all(Promise<D, F>... promises) {
        return JoinedPromise.of(promises);
    }
    
    @SafeVarargs
    public static <D, F extends Throwable> Promise<D, F> sequence(Promise<D, F>... promises) {
        return SequentialPromise.of(promises);
    }
}
