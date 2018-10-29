package org.joo.promise4j;

/**
 * Represents a pipe for rejected promise. Pipe is similar to filter, except
 * that they returns a {@link org.joo.promise4j.Promise} instead of the result.
 * 
 * @author griever
 *
 * @param <F> the exception type when the promise is rejected
 * @param <D_OUT> the response type after piping
 * @param <F_OUT> the exception type after piping
 */
public interface PipeFailureCallback<F extends Throwable, D_OUT, F_OUT extends Throwable> {

	/**
	 * A callback to be called when the promise is rejected.
	 * 
	 * @param cause the cause of the failure
	 * @return the new promise
	 */
	public Promise<D_OUT, F_OUT> onFail(final F cause);
}
