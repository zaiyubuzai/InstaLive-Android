@file:JvmName("Observers")
package com.venus.framework.util.rx

import com.venus.framework.util.L
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.observers.DisposableObserver
import io.reactivex.observers.DisposableSingleObserver

/*
 * 各类Rx Observer的基类
 *
 * Created by ywu on 2018/3/1.
 */

/**
 * Empty implement of [DisposableObserver], for the convenience of sub-classing
 * Created by ywu on 14/11/26.
 */
open class SimpleObserver<T> : DisposableObserver<T>() {

    override fun onComplete() = Unit

    override fun onError(e: Throwable) {
        callback(e, null)
        L.e("observable emitted error", e)
    }

    override fun onNext(t: T) {
        callback(null, t)
    }

    // callback onNext or onError
    protected open fun callback(e: Throwable?, t: T?) {}
}

/** Default empty implementation of [DisposableSingleObserver] */
open class SimpleSingleObserver<T> : DisposableSingleObserver<T>() {

    override fun onSuccess(t: T) = Unit

    override fun onError(e: Throwable) = L.e(e)
}

/** Default empty implementation of [DisposableMaybeObserver] */
open class SimpleMaybeObserver<T> : DisposableMaybeObserver<T>() {

    override fun onSuccess(t: T) = Unit

    override fun onComplete() = Unit

    override fun onError(e: Throwable) = L.e(e)
}

/** Default empty implementation of [DisposableMaybeObserver] */
open class SimpleCompletableObserver : DisposableCompletableObserver() {

    override fun onComplete() = Unit

    override fun onError(e: Throwable) = L.e(e)
}

/** 构造忽略结果的[DisposableObserver] */
fun <T> ignored(): DisposableObserver<T> = SimpleObserver()

/** 忽略结果的[DisposableCompletableObserver] */
fun completableIgnored(): DisposableCompletableObserver = object : DisposableCompletableObserver() {
    override fun onComplete() = Unit
    override fun onError(e: Throwable) = L.e(e)
}

/** 构造忽略结果的[DisposableSingleObserver] */
fun <T> singleIgnored(): DisposableSingleObserver<T> = object : DisposableSingleObserver<T>() {
    override fun onSuccess(t: T) = Unit
    override fun onError(e: Throwable) = L.e(e)
}

/** 构造忽略结果的[DisposableMaybeObserver] */
fun <T> maybeIgnored(): DisposableMaybeObserver<T> = object : DisposableMaybeObserver<T>() {
    override fun onSuccess(t: T) = Unit
    override fun onComplete() = Unit
    override fun onError(e: Throwable) = L.e(e)
}
