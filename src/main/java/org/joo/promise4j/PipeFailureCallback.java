package org.joo.promise4j;

public interface PipeFailureCallback<F extends Throwable, D_OUT, F_OUT extends Throwable> {

    public Promise<D_OUT, F_OUT> onFail(final F cause);
}
