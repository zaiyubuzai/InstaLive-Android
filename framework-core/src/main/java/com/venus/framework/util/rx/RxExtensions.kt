@file:JvmName("RxExtensions")

package com.venus.framework.util.rx

import com.venus.framework.util.Optional
import io.reactivex.Single

/** 返回Unit的[Single] */
@JvmField val singleUnit: Single<Unit> = Single.never()

/** 创建一个Optional的Single */
fun <T> singleOptional(t: T): Single<Optional<T>> = Single.just(Optional.of(t))

/** 创建一个Empty Optional的Single */
fun <T> singleEmptyOptional(): Single<Optional<T>> = Single.just(Optional.empty())
