package com.venus.framework.util;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by ywu on 2016/12/21.
 */
public final class BundleBuilder {
    private final Bundle bundle;

    public static BundleBuilder create() {
        return new BundleBuilder();
    }

    private BundleBuilder() {
        this.bundle = new Bundle();
    }

    public BundleBuilder putInt(String key, int i) {
        bundle.putInt(key, i);
        return this;
    }

    public BundleBuilder putLong(String key, long l) {
        bundle.putLong(key, l);
        return this;
    }

    public BundleBuilder putDouble(String key, double d) {
        bundle.putDouble(key, d);
        return this;
    }

    public BundleBuilder putBoolean(String key, boolean b) {
        bundle.putBoolean(key, b);
        return this;
    }

    public BundleBuilder putString(String key, String s) {
        bundle.putString(key, s);
        return this;
    }

    public BundleBuilder putCharSequence(String key, CharSequence s) {
        bundle.putCharSequence(key, s);
        return this;
    }

    public BundleBuilder putSerializable(String key, Serializable v) {
        bundle.putSerializable(key, v);
        return this;
    }

    public BundleBuilder putParcelable(String key, Parcelable p) {
        bundle.putParcelable(key, p);
        return this;
    }

    public BundleBuilder putBundle(String key, Bundle b) {
        bundle.putBundle(key, b);
        return this;
    }

    @NonNull
    public Bundle build() {
        return bundle;
    }
}
