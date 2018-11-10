package org.joo.promise4j.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.joo.promise4j.AlwaysCallback;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.DoneCallback;
import org.joo.promise4j.FailCallback;
import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;

public class SimpleDonePromise<D, F extends Throwable> extends AbstractPromise<D, F> {

	private D result;

	public SimpleDonePromise(final D result) {
		this.result = result;
	}

	@Override
	public Promise<D, F> done(final DoneCallback<D> callback) {
		callback.onDone(result);
		return this;
	}

	@Override
	public Promise<D, F> fail(final FailCallback<F> callback) {
		return this;
	}

	@Override
	public Promise<D, F> always(AlwaysCallback<D, F> callback) {
		callback.onAlways(DeferredStatus.RESOLVED, result, null);
		return this;
	}

	@Override
	public D get() {
		return result;
	}

	@Override
	public D get(long timeout, TimeUnit unit) throws PromiseException, TimeoutException, InterruptedException {
		return get();
	}

	@Override
	public DeferredStatus getStatus() {
		return DeferredStatus.RESOLVED;
	}
}
