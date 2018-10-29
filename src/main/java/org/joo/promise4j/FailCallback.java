package org.joo.promise4j;

/**
 * Represents a fail callback
 * 
 * @author griever
 *
 * @param <F> the exception type when the promise is rejected
 */
public interface FailCallback<F extends Throwable> {

	/**
	 * A callback to be called when the promise is rejected
	 * 
	 * @param failedCause the cause of the failure
	 */
	public void onFail(final F failedCause);
}
