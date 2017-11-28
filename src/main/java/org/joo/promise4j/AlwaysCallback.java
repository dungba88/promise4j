package org.joo.promise4j;

public interface AlwaysCallback<D, F extends Throwable> {

    public void onAlways(final DeferredStatus status, final D result, final F cause);
}
