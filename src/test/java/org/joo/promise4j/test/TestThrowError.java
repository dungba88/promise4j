package org.joo.promise4j.test;

import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.joo.promise4j.impl.CompletableDeferredObject;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class TestThrowError {

    @Test(expected = NoClassDefFoundError.class)
    public void testThrowErrorWithPipeDone() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwError).then(Promise::of).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testThrowErrorWithFilterDone() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwError).map(r -> r).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testThrowErrorWithPipeFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwError).pipeFail(Promise::ofCause).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithPipeFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwException).pipeFail(ex -> Promise.ofCause(new IllegalArgumentException())).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithFilterFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwException).filterFail(IllegalArgumentException::new).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testThrowErrorWithFilterFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::throwError).filterFail(ex -> ex).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test
    public void testThrowErrorOnDone() throws Throwable {
        doSomething().done(TestThrowError::throwError).get();
    }

    @Test
    public void testThrowErrorOnFail() throws Throwable {
        doSomething().fail(TestThrowError::throwError).get();
    }

    @Test
    public void testThrowErrorOnAlways() throws Throwable {
        doSomething().always((s, r, e) -> throwError(r)).get();
    }

    private static Promise<Object, Exception> doSomething() {
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> null);
        return new CompletableDeferredObject<Object, Exception>(future).promise();
    }

    private static Promise<Object, Exception> throwError(Object obj) {
        throw new NoClassDefFoundError("error");
    }

    private static Promise<Object, Exception> throwException(Object obj) {
        throw new UnsupportedOperationException();
    }
}