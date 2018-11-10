package org.joo.promise4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Represents a Deferred object
 * 
 * @author griever
 *
 * @param <D> the response type when the deferred is resolved
 * @param <F> the exception type when the deferred is rejected
 */
public interface Deferred<D, F extends Throwable> {

	/**
	 * Register a timeout, if time has passed and promise is not rejected or
	 * resolved it will be rejected with TimeoutException.
	 * 
	 * @param callback the callback
	 * @return the current promise
	 */
	public Deferred<D, F> withTimeout(long timeout, TimeUnit unit, Supplier<F> exceptionSupplier);

	/**
	 * Resolve the deferred
	 * 
	 * @param result the result of the deferred
	 * @return the deferred itself
	 */
	public Deferred<D, F> resolve(final D result);

	/**
	 * Reject the deferred
	 * 
	 * @param failedCause the failure cause
	 * @return the deferred itself
	 */
	public Deferred<D, F> reject(final F failedCause);

	/**
	 * Get the associated promise with the current deferred
	 * 
	 * @return the associated promise
	 */
	public Promise<D, F> promise();
}
