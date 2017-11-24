package org.joo.promise4j.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.AsyncDeferredObject;
import org.joo.promise4j.impl.SyncDeferredObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestSingleThread {

    private Supplier<Deferred<Object, Throwable>> deferredSupplier;
    
    public TestSingleThread(Supplier<Deferred<Object, Throwable>> deferredSupplier) {
        this.deferredSupplier = deferredSupplier;
    }

    @Test
    public void testResolveBefore() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.resolve(1);
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        });
    }

    @Test
    public void testResolveAfter() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        });
        deferred.resolve(1);
    }

    @Test
    public void testRejectBefore() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.reject(new UnsupportedOperationException());
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        });
    }

    @Test
    public void testRejectAfter() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        });
        deferred.reject(new UnsupportedOperationException());
    }

    @Test
    public void testResolveTwice() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.assertEquals(1, response);
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        });
        deferred.resolve(1);
        try {
            deferred.resolve(1);
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }
    }

    @Test
    public void testRejectTwice() {
        Deferred<Object, Throwable> deferred = deferredSupplier.get();
        deferred.promise().done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        });
        deferred.reject(new UnsupportedOperationException());
        try {
            deferred.reject(new UnsupportedOperationException());
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }
    }
    
    @Parameters
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[] {(Supplier<Deferred<Object, Throwable>>) () -> new AsyncDeferredObject<>()});
        list.add(new Object[] {(Supplier<Deferred<Object, Throwable>>) () -> new SyncDeferredObject<>()});
        return list;
    }
}
