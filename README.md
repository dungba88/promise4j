# promise4j

[![License](https://img.shields.io/github/license/dungba88/promise4j.svg?maxAge=2592000)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/org.dungba/joo-promise4j.svg?maxAge=2592000)](http://mvnrepository.com/artifact/org.dungba/joo-promise4j)
[![Build Status](https://travis-ci.org/dungba88/promise4j.svg?branch=master)](https://travis-ci.org/dungba88/promise4j)
[![Coverage Status](https://coveralls.io/repos/github/dungba88/promise4j/badge.svg?branch=master)](https://coveralls.io/github/dungba88/promise4j?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e9ed4ade5bed42c5a711db92b5288ffc)](https://www.codacy.com/app/dungba88/promise4j?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dungba88/promise4j&amp;utm_campaign=Badge_Grade)

Simple deferred/promise framework for Java. It supplements the asynchronous capability of Java by introducing Javascript-style promise.

## what is promise

A promise is...well, a promise. Let say you ask somebody to do something for you. He is currently busy, but he *promises* he will do it *some unspecified time* in the future, and he will tell you once he finishes the job, or when he cannot do the job for you. Put it in techincal terms:

- The person you asks is called a *deferred object*. A deferred object will give you a *promise*
- The act of finishing the job is called *resolve*
- The act of rejecting the job is called *reject*

## install

Install with Maven:

```
<dependency>
    <groupId>org.dungba</groupId>
    <artifactId>joo-promise4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## how to use

First you have to construct a deferred object, which is an instance of `Deferred`. There is `AsyncDeferredObject` and `SyncDeferredObject` that is ready to use. The asynchronous version will use [spinlocks](https://en.wikipedia.org/wiki/Spinlock) to be thread-safe, while the synchronous version will make use of `synchronized` keyword. The synchronous version is deprecated, since it can cause deadlocks. To create an asynchronous deferred object:

```java
DeferredObject<SomeResponseClass, SomeExceptionClass> deferred = new AsyncDeferredObject<>();
```

Then you can pass it to the provider (the one who actually do the job), there you can call `resolve()` or `reject()`:

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
});
```

The done callback will be called when the provider call `resolve()` with a response, and the fail callback will called when `reject()` is called.

## simple versions

Sometimes, it's not necessary to use `AsynchronousDeferredObject` since you already have the callback, or the result in hand. By using simpler versions, you will eliminate all of the overheads introduced by spinlocks.

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

## limitations

Currently `promise4j` only supports 1 done callback and 1 fail callback per `Promise`. Adding more callbacks by calling multiple `done()` or `fail()` will lead to unexpected results.
