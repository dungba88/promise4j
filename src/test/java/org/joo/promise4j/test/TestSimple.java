package org.joo.promise4j.test;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.SimpleDeferredObject;
import org.joo.promise4j.impl.SimpleDonePromise;
import org.joo.promise4j.impl.SimpleFailurePromise;
import org.junit.Assert;
import org.junit.Test;

public class TestSimple {

    @Test
    public void testSimpleResolve() {
        Deferred<Object, Throwable> deferred = new SimpleDeferredObject<>(response -> {
            Assert.assertEquals(1, response);
        }, ex -> {
            Assert.fail(ex.getMessage());
        }, (status, resolve, reject) -> {
            Assert.assertEquals(DeferredStatus.RESOLVED, status);
            Assert.assertEquals(1, resolve);
        });
        deferred.resolve(1);

        try {
            deferred.resolve(1);
            Assert.fail("must fail");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }

        try {
            deferred.promise().done(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
        
        try {
            deferred.promise().always(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
    }

    @Test
    public void testSimpleReject() {
        Deferred<Object, Throwable> deferred = new SimpleDeferredObject<>(response -> {
            Assert.fail("Cannot be resolved");
        }, ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        }, (status, resolve, reject) -> {
            Assert.assertEquals(DeferredStatus.REJECTED, status);
            Assert.assertTrue(reject instanceof UnsupportedOperationException);
        });
        deferred.reject(new UnsupportedOperationException());

        try {
            deferred.reject(new UnsupportedOperationException());
            Assert.fail("must fail");
        } catch (IllegalStateException ex) {
            Assert.assertEquals("Deferred is already resolved or rejected", ex.getMessage());
        }

        try {
            deferred.promise().fail(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
    }

    @Test
    public void testSimplePipe() {
        Deferred<Object, Throwable> deferred = new SimpleDeferredObject<>(null, null);
        try {
            deferred.promise().pipeDone(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
        try {
            deferred.promise().pipeFail(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
    }

    @Test
    public void testSimpleFilter() {
        Deferred<Object, Throwable> deferred = new SimpleDeferredObject<>(null, null);
        try {
            deferred.promise().filterDone(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
        try {
            deferred.promise().filterFail(null);
            Assert.fail("must fail");
        } catch (UnsupportedOperationException ex) {
            Assert.assertEquals("Callback cannot be deferred in non-deferred mode", ex.getMessage());
        }
    }

    @Test
    public void testSimpleDone() {
        Promise<Object, Throwable> promise = new SimpleDonePromise<Object, Throwable>(1);
        promise.done(response -> {
            Assert.assertEquals(1, response);
        }).fail(ex -> {
            Assert.fail(ex.getMessage());
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.RESOLVED, status);
            Assert.assertNull(ex);
            Assert.assertEquals(1, response);
        });
    }

    @Test
    public void testSimpleFailure() {
        Promise<Object, Throwable> promise = new SimpleFailurePromise<Object, Throwable>(
                new UnsupportedOperationException());
        promise.done(response -> {
            Assert.fail("Cannot be resolved");
        }).fail(ex -> {
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        }).always((status, response, ex) -> {
            Assert.assertEquals(DeferredStatus.REJECTED, status);
            Assert.assertNull(response);
            Assert.assertTrue(ex instanceof UnsupportedOperationException);
        });
    }
}
