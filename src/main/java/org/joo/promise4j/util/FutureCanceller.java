package org.joo.promise4j.util;

import java.util.concurrent.Future;
import java.util.function.BiConsumer;

public class FutureCanceller implements BiConsumer<Object, Throwable> {

    private Future<?> future;

    public FutureCanceller(final Future<?> future) {
        this.future = future;
    }

    public void accept(Object ignore, Throwable ex) {
        if (ex == null && future != null && !future.isDone())
            future.cancel(false);
    }
}
