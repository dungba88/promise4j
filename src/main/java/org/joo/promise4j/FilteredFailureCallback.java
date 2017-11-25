package org.joo.promise4j;

public interface FilteredFailureCallback<F extends Throwable, F_OUT extends Throwable> {

    public F_OUT onFail(final F cause);
}
