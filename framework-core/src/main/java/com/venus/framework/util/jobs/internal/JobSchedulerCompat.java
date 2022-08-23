package com.venus.framework.util.jobs.internal;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.venus.framework.util.L;
import com.venus.framework.util.jobs.JobConstants;

/**
 * JobScheduler相关api在此封装, 避免在低版本设备上找不到相关类
 *
 * Created by ywu on 16/9/7.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class JobSchedulerCompat implements JobConstants {

    private JobSchedulerCompat() {
    }

    @NonNull
    public static PersistableBundleCompat toPersistableBundleCompat(@Nullable Bundle extras) {
        PersistableBundleCompat bundle = new PersistableBundleCompat();
        if (extras == null) {
            return bundle;
        }
        for (String k : extras.keySet()) {
            Object v = extras.get(k);
            if (v == null) {
                continue;
            }

            if (v instanceof Integer) {
                bundle.putInt(k, (Integer) v);
            } else if (v instanceof Long) {
                bundle.putLong(k, (Long) v);
            } else if (v instanceof String) {
                bundle.putString(k, (String) v);
            } else if (v instanceof Double) {
                bundle.putDouble(k, (Double) v);
            } else if (v instanceof PersistableBundleCompat) {
                bundle.putPersistableBundleCompat(k, (PersistableBundleCompat) v);
            } else {
                L.w("unsupported value type: key=%s, value=%s", k, v);
            }
        }

        return bundle;
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Bundle toBundle(@Nullable PersistableBundle extras) {
        if (extras == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        for (String k : extras.keySet()) {
            Object v = extras.get(k);
            if (v == null) {
                continue;
            }

            if (v instanceof Integer) {
                bundle.putInt(k, (Integer) v);
            } else if (v instanceof Long) {
                bundle.putLong(k, (Long) v);
            } else if (v instanceof String) {
                bundle.putString(k, (String) v);
            } else if (v instanceof Double) {
                bundle.putDouble(k, (Double) v);
            } else if (v instanceof PersistableBundle) {
                bundle.putBundle(k, toBundle((PersistableBundle) v));
            } else {
                L.w("unsupported value type: key=%s, value=%s", k, v);
            }
        }

        return bundle;
    }

    @Nullable
    public static Bundle toBundle(@Nullable PersistableBundleCompat extras) {
        if (extras == null) {
            return null;
        }

        Bundle bundle = new Bundle();
        for (String k : extras.keySet()) {
            Object v = extras.get(k);
            if (v == null) {
                continue;
            }

            if (v instanceof Integer) {
                bundle.putInt(k, (Integer) v);
            } else if (v instanceof Long) {
                bundle.putLong(k, (Long) v);
            } else if (v instanceof String) {
                bundle.putString(k, (String) v);
            } else if (v instanceof Double) {
                bundle.putDouble(k, (Double) v);
            } else if (v instanceof PersistableBundle) {
                bundle.putBundle(k, toBundle((PersistableBundle) v));
            } else {
                L.w("unsupported value type: key=%s, value=%s", k, v);
            }
        }

        return bundle;
    }

}
