package org.joo.promise4j;

/**
 * Represents a done callback
 * 
 * @author griever
 *
 * @param <D> the response type when the promise is fulfilled
 */
public interface DoneCallback<D> {

	/**
	 * A callback to be called when the promise is fulfilled
	 * 
	 * @param result the result of the promise
	 */
	public void onDone(final D result);
}
