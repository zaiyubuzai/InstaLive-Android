package com.venus.framework.util.rx;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.subjects.AsyncSubject;

/**
 * AsyncSubject的包装器, 用于资源异步job的唯一性检查
 * <p>
 * Created by ywu on 15/11/13.
 */
public class AsyncResourceSubject<T extends Comparable<T>> {

    /**
     * 以给定的资源创建实例
     */
    public static <T extends Comparable<T>> AsyncResourceSubject<T> create(@NonNull T resource) {
        return new AsyncResourceSubject<>(resource);
    }

    /**
     * 将一组包装器展开为一组Observable
     */
    public static Iterable<Observable<? extends Comparable>> toObservables(
            @NonNull Iterable<AsyncResourceSubject<? extends Comparable>> subjects) {
        ArrayList<Observable<? extends Comparable>> observables = new ArrayList<>();
        for (AsyncResourceSubject<? extends Comparable> subject : subjects) {
            observables.add(subject.asyncSubject);
        }
        return observables;
    }

    /**
     * 合并一组包装器为单一的一个Observable
     */
    public static Observable<? extends Comparable> merge(
            @NonNull Iterable<AsyncResourceSubject<? extends Comparable>> subjects) {
        return Observable.merge(toObservables(subjects));
    }

    /**
     * 剔除失败的job
     */
    public static void removeFailedJobs(
            @NonNull Iterable<AsyncResourceSubject<? extends Comparable>> subjects) {
        Iterator<AsyncResourceSubject<? extends Comparable>> itrJobs = subjects.iterator();
        while (itrJobs.hasNext()) {
            AsyncResourceSubject<? extends Comparable> job = itrJobs.next();
            if (job.isFailed()) {
                itrJobs.remove();
            }
        }
    }

    protected final AsyncSubject<T> asyncSubject;
    protected final T resource;
    private Consumer<Throwable> actionOnError;
    private boolean isFailed;

    protected AsyncResourceSubject(@NonNull T resource) {
        this.asyncSubject = AsyncSubject.create();
        this.resource = resource;
    }

    public AsyncSubject<T> getAsyncSubject() {
        return asyncSubject;
    }

    public T getResource() {
        return resource;
    }

    /**
     * 将此action lift至observable中,以便记录失败状态
     */
    public Consumer<Throwable> getActionOnError() {
        if (actionOnError == null) {
            actionOnError = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable e) {
                    isFailed = true;
                }
            };
        }

        return actionOnError;
    }

    public DisposableObserver<T> toObserver() {
        return new DisposableObserver<T>() {
            @Override
            public void onNext(T localVideoInfo) {
                asyncSubject.onNext(localVideoInfo);
            }

            @Override
            public void onError(Throwable e) {
                asyncSubject.onError(e);
            }

            @Override
            public void onComplete() {
                asyncSubject.onComplete();
            }
        };
    }

    public boolean isFailed() {
        return isFailed;
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AsyncResourceSubject && resource.equals(((AsyncResourceSubject)o).resource);
    }
}
