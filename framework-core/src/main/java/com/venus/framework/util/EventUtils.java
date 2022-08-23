package com.venus.framework.util;

import android.os.Bundle;
import android.os.Message;

import com.external.eventbus.EventBus;

/**
 * EventBus utilities
 * Created by ywu on 14-10-12.
 */
public final class EventUtils {

    private EventUtils() {

    }

    public static void register(Object l) {
        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(l)) {
            bus.register(l);
        }
    }

    public static void registerSticky(Object l) {
        EventBus bus = EventBus.getDefault();
        if (!bus.isRegistered(l)) {
            bus.registerSticky(l);
        }
    }

    public static void unregister(Object l) {
        EventBus bus = EventBus.getDefault();
        if (bus.isRegistered(l)) {
            bus.unregister(l);
        }
    }

    public static void post(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        EventBus.getDefault().post(msg);
    }

    public static void post(int what, int arg1, int arg2) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        EventBus.getDefault().post(msg);
    }

    public static void post(int what, Bundle data) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.setData(data);
        EventBus.getDefault().post(msg);
    }

    public static void post(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        EventBus.getDefault().post(msg);
    }

    public static void post(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        EventBus.getDefault().post(msg);
    }

    public static void post(int what, int arg1, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.obj = obj;
        EventBus.getDefault().post(msg);
    }

    public static void postSticky(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        EventBus.getDefault().postSticky(msg);
    }

    public static void postSticky(int what, int arg1) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        EventBus.getDefault().postSticky(msg);
    }

    public static void postSticky(int what, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.obj = obj;
        EventBus.getDefault().postSticky(msg);
    }
}
