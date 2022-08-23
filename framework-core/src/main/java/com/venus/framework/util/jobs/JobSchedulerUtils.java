package com.venus.framework.util.jobs;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.Task;
import com.venus.framework.util.L;
import com.venus.framework.util.jobs.internal.JobSchedulerCompat;

import java.util.concurrent.TimeUnit;

/**
 * 后台作业调度器, 可适配系统版本的差异
 * <p>
 * Created by ywu on 16/9/6.
 */
public final class JobSchedulerUtils implements JobConstants {

    private JobSchedulerUtils() {
    }

    public static boolean isEnabled(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || isGcmAvailable(context);
    }

    /**
     * Schedule一个Job.
     *
     * @param jobIntent Job的定义
     */
    public static void schedule(@NonNull JobIntent jobIntent) {
        //提供了tag 用evernote job
        if (!TextUtils.isEmpty(jobIntent.getTag())) {
            scheduleEvernoteJob(jobIntent);
        }
    }

    private static void scheduleEvernoteJob(@NonNull JobIntent jobIntent) {
        try {
            JobRequest.Builder builder = new JobRequest.Builder(jobIntent.getTag());
            Bundle extras = jobIntent.getExtras();
            extras.putString(EXTRA_JOB_ACTION, jobIntent.getAction());

            if (jobIntent.isPeriodic()) {
                final long intervalMillis = jobIntent.getIntervalMillis();
                builder.setPeriodic(intervalMillis, intervalMillis / 4);
            } else {
                builder.setExecutionWindow(1, TimeUnit.MINUTES.toMillis(15));
            }

            PersistableBundleCompat persistableBundleCompat = JobSchedulerCompat.toPersistableBundleCompat(jobIntent.getExtras());
            persistableBundleCompat.putInt(JobIntent.JOB_ID, jobIntent.getJobId());
            builder.setExtras(persistableBundleCompat);
            builder.setRequiredNetworkType(getNetworkType(jobIntent.getNetworkType()));
            builder.build().schedule();
        } catch (Throwable e) {
            L.e(e);
        }

    }

    private static JobRequest.NetworkType getNetworkType(int networkType) {
        switch (networkType) {
            case JobConstants.NETWORK_TYPE_UNSPECIFIED:
                return JobRequest.NetworkType.ANY;
            case JobConstants.NETWORK_TYPE_CONNECTED:
                return JobRequest.NetworkType.CONNECTED;
            case JobConstants.NETWORK_TYPE_UNMETERED:
                return JobRequest.NetworkType.UNMETERED;
        }
        return JobRequest.NetworkType.ANY;
    }

    // 判断play service是否可用
    private static boolean isGcmAvailable(@NonNull Context context) {
        try {
            return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(context);
        } catch (Exception e) {
            return false;
        }
    }

    private static int getGcmNetworkType(@NetworkType int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_UNSPECIFIED:
                return Task.NETWORK_STATE_ANY;
            case NETWORK_TYPE_CONNECTED:
                return Task.NETWORK_STATE_CONNECTED;
            case NETWORK_TYPE_UNMETERED:
                return Task.NETWORK_STATE_UNMETERED;
            default:
                return Task.NETWORK_STATE_CONNECTED;
        }
    }
}
