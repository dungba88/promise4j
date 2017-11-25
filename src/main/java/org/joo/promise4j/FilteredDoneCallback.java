package org.joo.promise4j;

public interface FilteredDoneCallback<D, D_OUT> {

    public D_OUT onDone(final D result);
}
