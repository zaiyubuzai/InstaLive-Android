package com.venus.framework.util.rx;

import androidx.annotation.Nullable;

import io.reactivex.functions.Function;
import kotlin.Unit;

/**
 * Generic RX callback
 * <p/>
 * Created by ywu on 16/5/9.
 */
public abstract class RxCallback implements Function<Throwable, Unit> {

    @Override
    public Unit apply(@Nullable Throwable e) throws Exception {
        call(e);
        return null;
    }

    /**
     * @deprecated use #apply instead
     */
    public abstract void call(Throwable e);
}
