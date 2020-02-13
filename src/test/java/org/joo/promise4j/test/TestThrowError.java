package org.joo.promise4j.test;

import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.junit.Test;

public class TestThrowError {

    @Test(expected = NoClassDefFoundError.class)
    public void testThrowErrorWithPipeDone() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowError).then(Promise::of).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testThrowErrorWithFilterDone() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowError).map(r -> r).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testThrowErrorWithPipeFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowError).pipeFail(Promise::ofCause).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithPipeFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowException).pipeFail(ex -> Promise.ofCause(new IllegalArgumentException())).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowExceptionWithFilterFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowException).filterFail(IllegalArgumentException::new).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    @Test(expected = ClassCastException.class)
    public void testThrowErrorWithFilterFail() throws Throwable {
        try {
            doSomething().then(TestThrowError::thowError).filterFail(ex -> ex).get();
        } catch (PromiseException ex) {
            throw ex.getCause();
        }
    }

    private static Promise<Object, Exception> doSomething() {
        return Promise.of(null);
    }

    private static Promise<Object, Exception> thowError(Object obj) {
        throw new NoClassDefFoundError("error");
    }

    private static Promise<Object, Exception> thowException(Object obj) {
        throw new UnsupportedOperationException();
    }
}