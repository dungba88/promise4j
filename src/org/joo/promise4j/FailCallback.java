package org.joo.promise4j;

public interface FailCallback<F extends Throwable> {

    public void onFail(final F failedCause);
}
