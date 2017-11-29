# promise4j

[![License](https://img.shields.io/github/license/dungba88/promise4j.svg?maxAge=2592000)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dungba/joo-promise4j.svg?maxAge=604800)](http://mvnrepository.com/artifact/org.dungba/joo-promise4j)
[![Build Status](https://travis-ci.org/dungba88/promise4j.svg?branch=master)](https://travis-ci.org/dungba88/promise4j)
[![Coverage Status](https://coveralls.io/repos/github/dungba88/promise4j/badge.svg?branch=master)](https://coveralls.io/github/dungba88/promise4j?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e9ed4ade5bed42c5a711db92b5288ffc)](https://www.codacy.com/app/dungba88/promise4j?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dungba88/promise4j&amp;utm_campaign=Badge_Grade)

Simple deferred/promise framework for Java. It supplements the asynchronous capability of Java by introducing Javascript-style promise.

## table of contents

- [what is promise](#what-is-promise)
- [install](#install)
- [how to use](#how-to-use)
- [advanced topics](#advanced-topics)
    - [pip and filter](#pipe-and-filter)
    - [joined promise](#joined-promise)
    - [simple versions](#simple-versions)
    - [limitations](#limitations)
- [license](#license)

## what is promise

A promise is...well, a promise. Let say you ask somebody to do something for you. He *might* be busy at the moment, but he *promises* he will do it *some unspecified time* in the future, and he will tell you once he finishes the job, or when he cannot do it for you. Put it in techincal terms:

- The person you asks is called a *deferred object*. A deferred object will give you a *promise*
- The act of fulfilling the job is called *resolve*
- The act of rejecting the job is called *reject*
- You use *callbacks* to wait for the result

## install

Install with Maven:

```
<dependency>
    <groupId>org.dungba</groupId>
    <artifactId>joo-promise4j</artifactId>
    <version>1.1.0</version>
</dependency>
```

## how to use

First you have to construct a deferred object, which is an instance of `Deferred`. There is `AsyncDeferredObject`, `SyncDeferredObject` and `CompletableDeferredObject` that is ready to use. The asynchronous version will use [spinlocks](https://en.wikipedia.org/wiki/Spinlock) to be thread-safe, while the synchronous version will make use of `synchronized` keyword. The completable version uses Java 8 `CompletableFuture` and can support multi-callbacks. The synchronous version is not favored, since it can cause deadlocks.

To create an asynchronous deferred object:

```java
DeferredObject<SomeResponseClass, SomeExceptionClass> deferred = new AsyncDeferredObject<>();
```

Same for other deferred type. Then you can pass it to the provider (the one who actually do the job), there you can call `resolve()` or `reject()`:

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

The done callback will be called when the provider call `resolve()` with a response, and the fail callback will called when `reject()` is called. Since `1.1.0`, you can use AlwaysCallback to be notified when the promise completes, regardless whether it is done or failed.

## advanced topics

### pipe and filter

You can also chain the processing via `pipeDone()`, `pipeFail`, `filterDone()` and `filterFail()`:

```java
deferred.promise().pipeDone(response -> {
    // this will be called only when the original deferred resolved successfully
    // it will create new stage
    return somePromise;
}).pipeDone(response -> {
    // this will be called only when the preceding executed stage resolve successfully
    // it will create new stage
    return somePromise;
}).filterDone(response -> {
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
}).done(response -> {
    // this will be called only when the preceding executed stage resolve successfully
    // it will not create any stage
}).fail(response -> {
    // this will be called only when the preceding executed stage is rejected
    // it will not create any stage
}).pipeDone(... // do it all again if you wish);
```

*Note on exception type*

If any exception is thrown while executing the preceding stage, that stage is considered rejected with the thrown exception as cause. So be careful with the type of the exception you received, it might be the type of the preceding stage's promise failPipe/failFilter or it also can be the type of the thrown exception. For example:

```java
...pipeDone(response -> {
    if (...)    // some condition that raise the exception
        throw new IllegalArgumentException();
    return new SimpleFailurePromise(new UnsupportedOperationException());
}).pipeFail(ex -> {
    // ex can be of type IllegalArgumentException or UnsupportedOperationException
});
```

Best practice is that you always be consistent in the exception type and try not to throw exception. There are two ways you can achieve this:

1. Wrap your pipe handler with a try-catch block and reject the promise yourself with the same exception type:

```java
...pipeDone(response -> {
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
...pipeDone((PipeDoneCallback<Integer, Integer, Exception>)response -> {
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

### simple versions

Sometimes, it's not necessary to use `AsyncDeferredObject` or `CompletableDeferredObject` since you already have the callback, or the result in hand. By using simpler versions, you will eliminate all of the overheads introduced by spinlocks.

1. If you already have the done/fail callback and not intend to assign the callback later:

```java
DeferredObject<SomeResponseClass, SomeExceptionClass> deferred = new SimpleDeferredObject<>(doneCallback, failCallback);

// resolve or reject as usual
...
```

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
