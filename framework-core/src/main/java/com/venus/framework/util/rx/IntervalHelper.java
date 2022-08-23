package com.venus.framework.util.rx;

import androidx.annotation.NonNull;

import com.venus.framework.util.L;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

/**
 * Created by ywu on 2017/2/15.
 */

public class IntervalHelper {

    private final Observable<Long> timer;
    private final List<WeakReference<IntervalObserver>> observers;
    private Disposable disposable;

    public IntervalHelper(long interval, TimeUnit unit) {
        this(interval, unit, null);
    }

    public IntervalHelper(long interval, TimeUnit unit, Scheduler scheduler) {
        if (scheduler != null) {
            this.timer = Observable.interval(interval, unit, scheduler)
                    .observeOn(RxSchedulers.ui());
        } else {
            this.timer = Observable.interval(interval, unit)
                    .observeOn(RxSchedulers.ui());
        }

        this.observers = new ArrayList<>();
    }

    public void subscribe(@NonNull final IntervalObserver observer) {
        observers.add(new WeakReference<>(observer));
        doSubscribe();
    }

    private void doSubscribe() {
        if (disposable == null || disposable.isDisposed()) {
            disposable = timer.subscribeWith(new SimpleObserver<Long>() {
                @Override
                public void onNext(Long t) {
                    super.onNext(t);
                    emitTick(t);
                }
            });
        }
    }

    private void emitTick(long t) {
        Iterator<WeakReference<IntervalObserver>> itr = observers.iterator();
        while (itr.hasNext()) {
            WeakReference<IntervalObserver> ref = itr.next();
            if (ref.get() != null) {
                emitTick(t, ref);
            } else {
                itr.remove();
            }
        }
    }

    private void emitTick(long t, WeakReference<IntervalObserver> ref) {
        try {
            IntervalObserver o = ref.get();
            if (o != null) {
                o.onTick(t);
            }
        } catch (Exception e) {
            L.e(e);
        }
    }

    public void resume() {
        doSubscribe();
        emitTick(0);  // resume之后立即发出一次事件
    }

    public void pause() {
        try {
            if (disposable != null) {
                disposable.dispose();
                disposable = null;
            }
        } catch (Exception e) {
            L.e(e);
        }
    }

    public void clear() {
        pause();
        observers.clear();
    }
}
