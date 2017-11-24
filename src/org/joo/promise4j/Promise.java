package org.joo.promise4j;

public interface Promise<D, F extends Throwable> {

    public Promise<D, F> done(DoneCallback<D> callback);

    public Promise<D, F> fail(FailCallback<F> callback);
}
