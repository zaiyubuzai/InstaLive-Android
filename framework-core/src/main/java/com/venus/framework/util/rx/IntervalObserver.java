package com.venus.framework.util.rx;

/**
 * Created by ywu on 2017/2/15.
 */

public interface IntervalObserver {
    void onTick(long t) throws Exception;
}
