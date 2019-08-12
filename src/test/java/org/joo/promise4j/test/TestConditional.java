package org.joo.promise4j.test;

import org.joo.promise4j.Promise;
import org.joo.promise4j.PromiseException;
import org.junit.Assert;
import org.junit.Test;

public class TestConditional {

    @Test
    public void testConditional() throws PromiseException, InterruptedException {
        Promise<String, Exception> promise = Promise.of("test1");
        var result = promise.when("test"::equals, r -> Promise.of("test2")) //
                            .when("test1"::equals, r -> Promise.of("test3")) //
                            .get();
        Assert.assertEquals("test3", result);

        result = promise.when("test"::equals, r -> Promise.of("test2")) //
                        .when("test1"::equals, r -> Promise.of("test3")) //
                        .when("test3"::equals, r -> Promise.of("test4")) //
                        .get();
        Assert.assertEquals("test4", result);

        result = promise.when("test"::equals, r -> Promise.of("test2")) //
                        .when("test1"::equals, r -> Promise.of("test3")) //
                        .when("test4"::equals, r -> Promise.of("test5")) //
                        .get();
        Assert.assertEquals("test3", result);

        result = promise.when("test"::equals, r -> Promise.of("test2")) //
                        .when("test2"::equals, r -> Promise.of("test3")) //
                        .when("test3"::equals, r -> Promise.of("test4")) //
                        .get();
        Assert.assertEquals("test1", result);

        result = promise.when("test1"::equals, r -> Promise.of("test2")) //
                        .when("test1"::equals, r -> Promise.of("test3")) //
                        .map(r -> "test4") //
                        .when("test4"::equals, r -> Promise.of("test5")) //
                        .get();
        Assert.assertEquals("test5", result);
    }
}
