package com.venus.framework.util;

/**
 * 记录debug信息，便于查找线上问题，由app层提供实现
 * <p>
 * 为与Logging区分，起名Tracker，可考虑把统计性的事件跟踪行为也定义在这
 * <p>
 * Created by ywu on 2017/3/10.
 */
public interface Tracker {

    /**
     * 记录debug信息,目前用来帮助查找线上问题
     */
    void log(String msg, Object... args);

    /**
     * 记录一条诊断信息,目前用来帮助查找线上问题
     */
    void logException(Throwable e);
}
