package org.joo.promise4j;

public interface PipeDoneCallback<D, D_OUT, F_OUT extends Throwable> {

    public Promise<D_OUT, F_OUT> onDone(final D result);
}
