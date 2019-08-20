# promise4j

[![Maven Central](https://img.shields.io/maven-central/v/org.dungba/joo-promise4j.svg?maxAge=604800)](http://mvnrepository.com/artifact/org.dungba/joo-promise4j)
[![Javadocs](http://javadoc.io/badge/org.dungba/joo-promise4j.svg)](http://javadoc.io/doc/org.dungba/joo-promise4j)
[![Build Status](https://travis-ci.org/dungba88/promise4j.svg?branch=master)](https://travis-ci.org/dungba88/promise4j)
[![Coverage Status](https://coveralls.io/repos/github/dungba88/promise4j/badge.svg?branch=master)](https://coveralls.io/github/dungba88/promise4j?branch=master)

Fluent deferred/promise framework for Java with minimal dependencies. It supplements the asynchronous capability of Java by introducing Javascript-style promise (join, pipe, filter, etc.). It only depends on [net.jodah/failsafe](https://mvnrepository.com/artifact/net.jodah/failsafe) for retry purpose.

## table of contents

- [what is promise](#what-is-promise)
- [install](#install)
- [how to use](#how-to-use)
- [advanced topics](#advanced-topics)
    - [pip and filter](#pipe-and-filter)
    - [joined promise](#joined-promise)
    - [retry](#retry)
    - [simple versions](#simple-versions)
    - [limitations](#limitations)
- [license](#license)

## what is promise

A promise is...well, a promise. Let say you ask somebody to do something for you. He *might* be busy at the moment, but he *promises* he will do it *some unspecified time* in the future, and he will tell you once he finishes the job, or reject when he cannot do it for you. Put it in techincal terms:

- The person you asks is called a *deferred object*. A deferred object will give you a *promise*
- The act of fulfilling the job is called *resolve*
- The act of rejecting the job is called *reject*
- You use *callbacks* to handle for the result

## install

Install with Maven:

```xml
<dependency>
    <groupId>org.dungba</groupId>
    <artifactId>joo-promise4j</artifactId>
    <version><!-- latest version. see above --></version>
</dependency>
```

## how to use

First you have to construct a deferred object, which is an instance of `Deferred`. There is `AsyncDeferredObject`, `SyncDeferredObject` and `CompletableDeferredObject` that is ready to use. The asynchronous version will use [spinlocks](https://en.wikipedia.org/wiki/Spinlock) to be thread-safe, while the synchronous version will make use of `synchronized` keyword. The completable version uses Java 8 `CompletableFuture` and can support multi-callbacks. The synchronous version is not favored, since it can cause deadlocks.

To create an asynchronous deferred object:

```java
DeferredObject<SomeResponseClass, SomeExceptionClass> deferred = new AsyncDeferredObject<>();
```

After that you can pass it to the provider (the one who actually do the job), and call `resolve()` or `reject()` on the deferred object:

```java
try {
    SomeResponseClass someResponse = ... // get the response
    deferred.resolve(someResponse); // resolve the job
} catch(SomeExceptionClass ex) {
    deferred.reject(ex);  // cannot fulfill the job, reject it
}
```

In your consumer, you can call `promise()` to get the promise, and wait for its result:

```java
deferred.promise().done(response -> {
    // do something with the response
}).fail(ex -> {
    // do something with the exception
}).always((status, response, ex) -> {   // available since 1.1.0
    // this will always be called
});
```

*Note: It's actually better to pass the promise to the consumer, since they don't need to care about the deferred object*

The done callback will be invoked when `resolve()` is called with a response, and the fail callback will be invoked when `reject()` is called with an exception. Since `1.1.0`, you can use AlwaysCallback to be notified when the promise completes, regardless whether it is resolved or rejected.

`done`, `fail` and `always` will return the same promise so that you can chain them together, creating a *fluent* programming.

## advanced topics

### pipe and filter

You can also chain the processing via `then()` (formerly `pipeDone()`), `pipeFail`, `map()` (formerly `filterDone()`) and `filterFail()`:

```java
deferred.promise().then(response -> {
    // this will be called only when the original deferred resolved successfully
    // it will create new stage
    return somePromise;
}).then(response -> {
    // this will be called only when the preceding executed stage resolve successfully
    // it will create new stage
    return somePromise;
}).map(response -> {
    // this will be called only when the preceeding executed stage resolve successfully
    // it will create new stage
    return someResponse;
}).pipeFail(ex -> {
    // this will be called only when the preceding executed stage is rejected
    // it will create new stage
    return somePromise;
}).filterFail(ex -> {
    // this will be called only when the preceding executed stage is rejected
    // it will create new stage
    return someException;
}).then((status, result, ex) -> {
    // same as always() but allow you to chain
    return somePromise;
}).done(response -> {
    // this will be called only when the preceding executed stage resolve successfully
    // it will not create any stage
}).fail(response -> {
    // this will be called only when the preceding executed stage is rejected
    // it will not create any stage
}).then(... // do it all again if you wish);
```

*Note on exception type*

If any exception is thrown while executing the preceding stage, that stage is considered rejected with the thrown exception as cause. So be careful with the type of the exception you received, it might be the type of the preceding stage's promise failPipe/failFilter or it also can be the type of the thrown exception. For example:

```java
...then(response -> {
    if (...)    // some condition that raise the exception
        throw new IllegalArgumentException();
    return new SimpleFailurePromise(new UnsupportedOperationException());
}).pipeFail(ex -> {
    // ex can be of type IllegalArgumentException or UnsupportedOperationException
});
```

Best practice is that you always be consistent in the exception type and try not to throw exception. There are two ways you can achieve this:

1. Wrap your pipe handler with a try-catch block and reject the promise yourself with the expected exception type:

```java
...then(response -> {
    try {
        if (...)    // some condition that raise the exception
            throw new IllegalArgumentException();
        return new SimpleFailurePromise(new UnsupportedOperationException());
    } catch (Exception ex) {
        // convert ex to correct exception type
        // this can be done by using ex as the cause of the expected exception
        // e.g: expectedException = new UnsupportedOperationException(ex);
        return new SimpleFailurePromise(expectedException);
    }
}).pipeFail(ex -> {
    // ex can only be of type UnsupportedOperationException
});
```

2. Explicitly use a PipeDoneCallback<ANY_TYPE, ANY_TYPE, Exception> or PipeFailureCallback<ANY_TYPE, ANY_TYPE, Exception> to cover all exception types.

```java
...then((PipeDoneCallback<Integer, Integer, Exception>)response -> {
    // return promise of Exception type
}).pipeFail(ex -> {
    // ex can be of any type here
});
```

Although with the second approach you don't have to add a try-catch block, it tends to be more error-prone since you have no way of knowing exception type beforehand in the `failCallback`. You may also need to cast your promise to *raw and unchecked* `(Promise)` type if your promise is incompatible with `Exception`:

```java
@SuppressWarnings({ "unchecked", "rawtypes" })
return (Promise)someIncompatiblePromise;
```

### joined promise

Since `1.1.0`, you can make use of `JoinedPromise` to join multiple promises into a single one.

```java
promise = JoinedPromise.from(promise1, promise2, promise3);
promise.done(...).fail(...);
```

The conditions for callbacks are as below:
- The joined promise will be considered fulfilled if and only if *all* child promises are resolved successfully
- The joined promise will be considered rejected if *at least one* child promise is rejected
- The joined promise will be considered completed if *all* child promises are either resolved or rejected

The fail callback will be triggered only once for the first rejected child promise. Any other failure are ignore.

### retry

Since `1.1.1`, retry is supported with `FailSafePromise`:

```java
// create a retry policy to retry at most 3 times and 100 milliseconds delay between each retry
RetryPolicy retryPolicy = new RetryPolicy().retryOn(IllegalStateException.class)
    .withDelay(100, TimeUnit.MILLISECONDS).withMaxRetries(3);

// construct the promise from response supplier
Promise promise = FailSafePromise.from(() -> {
    // some logic that might cause exception
    return someResponse;
}, Failsafe.with(retryPolicy).with(executor));

// or construct from promise supplier
Promise promise = FailSafePromise.fromPromise(() -> {
    // some logic that might cause failure
    return somePromise;
}, Failsafe.with(retryPolicy).with(executor));

// use the promise as usual
promise.done(...).fail(...).then(...);
```

The `RetryPolicy`, `FailSafe` is coming from [@jhalterman/failsafe](https://github.com/jhalterman/failsafe). You can refer to their manual for more details. The promise will only accept a `AsyncFailSafe` (by calling `.with(executor)`). A retry will be triggered to the `FailSafe` engine if one the following conditions are satisfied:

- You call `.from(...)` and an exception is thrown
- You call `.fromPromise(...)` and either an exception is thrown, or you explicitly reject the returned promise

Note that even if the above conditions are true, retry might not happen because the policy you set doesn't match. These are managed by the `FailSafe` engine itself (again, refer to their manual).

### simple versions

Sometimes, it's not necessary to use `AsyncDeferredObject` or `CompletableDeferredObject` since you already have the callback, or the result in hand. By using simpler versions, you will eliminate all of the overheads introduced by spinlocks.

1. If you already have the done/fail callback and not intend to assign the callback later:

```java
DeferredObject<SomeResponseClass, SomeExceptionClass> deferred = new SimpleDeferredObject<>(doneCallback, failCallback);

// resolve or reject as usual
...
```

*Note that you cannot assign the callback later if you use `SimpleDeferredObject`. A `UnsupportedOperationException` will be thrown if you try to do so.*

2. If you already have the response, you don't even need to create a `Deferred`!:

```java
SimpleDonePromise promise = new SimpleDonePromise(response);

// register done callback as usual
promise.done(response -> {
    // do something with the response
});
```

Same for rejecting case, you will use `SimpleFailurePromise`

*All simple versions are not thread-safe and should be used with cautions*

### limitations

Currently `AsyncDeferredObject` and `SyncDeferredObject` only supports 1 done callback and 1 fail callback per `Promise`. Adding more callbacks by calling multiple `done()` or `fail()` will lead to unexpected results. Only `CompletableDeferredObject` will support multi-callbacks.

**Deadlocks with SyncDeferredObject**

There are cases where `SyncDeferredObject` can cause deadlock. Because it uses `synchronized` so the thread registering the callback (calls `promise.done(...)`) and the thread resolving the deferred (calls `deferred.resolve(...)`) will have to wait on the same lock. If they again both wait for another lock, then deadlock might happen. So you should use it with cautions and make sure they don't wait on any other lock. This might not be obvious since it depends on the framework/platform you use.

## license

This library is distributed under MIT license, see [LICENSE](LICENSE)
