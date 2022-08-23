package com.venus.framework.util.rx

import androidx.annotation.VisibleForTesting
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Rx scheduler utils
 *
 * Created by ywu on 2016/11/22.
 */
object RxSchedulers {

    @JvmStatic @VisibleForTesting
    var testing: Boolean = false

    private val appSingleExecutor by lazy { Executors.newSingleThreadExecutor() }

    @JvmStatic fun computation(): Scheduler = if (testing)
        Schedulers.trampoline() else Schedulers.computation()

    @JvmStatic fun io(): Scheduler = if (testing)
        Schedulers.trampoline() else Schedulers.io()

    @JvmStatic fun ui(): Scheduler = if (testing)
        Schedulers.trampoline() else AndroidSchedulers.mainThread()

    @JvmStatic fun single(): Scheduler = if (testing)
        Schedulers.trampoline() else from(Executors.newSingleThreadExecutor())

    /**
     * Application生命周期单一线程的调度器
     */
    @JvmStatic fun appSingle(): Scheduler = if (testing)
        Schedulers.trampoline() else from(appSingleExecutor)

    @JvmStatic fun from(executor: Executor): Scheduler = if (testing)
        Schedulers.trampoline() else Schedulers.from(executor)

    /**
     * Scheduling a most common case of Rx job, running in background (I/O), observed on main thread
     */
    @JvmStatic fun <T> Observable<T>.schedule(): Observable<T> = subscribeOn(io()).observeOn(ui())

    /**
     * Scheduling a most common case of Rx job, running in background (I/O), observed on main thread
     */
    @JvmStatic fun <T> Single<T>.schedule(): Single<T> = subscribeOn(io()).observeOn(ui())

    /**
     * Scheduling a most common case of Rx job, running in background (I/O), observed on main thread
     */
    @JvmStatic fun <T> Maybe<T>.schedule(): Maybe<T> = subscribeOn(io()).observeOn(ui())

    /**
     * Scheduling a most common case of Rx job, running in background (I/O), observed on main thread
     */
    @JvmStatic fun Completable.schedule(): Completable = subscribeOn(io()).observeOn(ui())

    /**
     * Scheduling a Rx job running and observed on I/O threads
     */
    @JvmStatic fun <T> Observable<T>.scheduleIo(): Observable<T> = subscribeOn(io())

    /**
     * Scheduling a Rx job running and observed on I/O threads
     */
    @JvmStatic fun <T> Single<T>.scheduleIo(): Single<T> = subscribeOn(io())

    /**
     * Scheduling a Rx job running and observed on I/O threads
     */
    @JvmStatic fun <T> Maybe<T>.scheduleIo(): Maybe<T> = subscribeOn(io())

    /**
     * Scheduling a Rx job running and observed on I/O threads
     */
    @JvmStatic fun Completable.scheduleIo(): Completable = subscribeOn(io())

    /**
     * Scheduling a Rx job running and observed on computation threads
     */
    @JvmStatic fun <T> Observable<T>.scheduleComputation(): Observable<T> = subscribeOn(computation())

    /**
     * Scheduling a Rx job running and observed on computation threads
     */
    @JvmStatic fun <T> Single<T>.scheduleComputation(): Single<T> = subscribeOn(computation())

    /**
     * Scheduling a Rx job running and observed on computation threads
     */
    @JvmStatic fun <T> Maybe<T>.scheduleComputation(): Maybe<T> = subscribeOn(computation())

    /**
     * Scheduling a Rx job running and observed on computation threads
     */
    @JvmStatic fun Completable.scheduleComputation(): Completable = subscribeOn(computation())

    /**
     * Scheduling a Rx job running and observed on the app lifecycle single thread
     */
    @JvmStatic fun <T> Observable<T>.scheduleAppSingle(): Observable<T> = subscribeOn(appSingle())

    /**
     * Scheduling a Rx job running and observed on the app lifecycle single thread
     */
    @JvmStatic fun <T> Single<T>.scheduleAppSingle(): Single<T> = subscribeOn(appSingle())

    /**
     * Scheduling a Rx job running and observed on the app lifecycle single thread
     */
    @JvmStatic fun <T> Maybe<T>.scheduleAppSingle(): Maybe<T> = subscribeOn(appSingle())

    /**
     * Scheduling a Rx job running and observed on the app lifecycle single thread
     */
    @JvmStatic fun Completable.scheduleAppSingle(): Completable = subscribeOn(appSingle())
}
