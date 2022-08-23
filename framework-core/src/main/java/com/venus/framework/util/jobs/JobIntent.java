package com.venus.framework.util.jobs;

import android.annotation.TargetApi;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmTaskService;

/**
 * 封装一个Job的信息,
 * <p>
 * Created by ywu on 16/9/7.
 */
public class JobIntent {

    public static final String JOB_ID = "job_id";

    private Context context;

    private String tag;

    private int jobId;

    private String action;

    @NetworkType
    private int networkType;

    private boolean isPeriodic;

    private long intervalMillis;

    private boolean isPersisted;

    @Nullable
    private Bundle extras;

    // for SDK 21+
    private Class<? extends JobService> jobService;

    // for SDK before 21
    private Class<? extends GcmTaskService> taskService;

    public Context getContext() {
        return context;
    }

    public JobIntent setContext(@NonNull Context context) {
        this.context = context;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getJobId() {
        return jobId;
    }

    /**
     * 设置唯一的Job ID
     */
    public JobIntent setJobId(int jobId) {
        this.jobId = jobId;
        return this;
    }

    public String getAction() {
        return action;
    }

    /**
     * 可选的Action.
     * <p>
     * Job运行时可从params以EXTRA_JOB_ACTION为key取出
     */
    public JobIntent setAction(String action) {
        this.action = action;
        return this;
    }

    public JobIntent putIntent(@Nullable Intent intent) {
        if (intent != null) {
            action = intent.getAction();
            putAllExtras(intent.getExtras());
        }

        return this;
    }

    @NetworkType
    public int getNetworkType() {
        return networkType;
    }

    /**
     * Job运行时应满足的网络状态
     */
    public JobIntent setNetworkType(@NetworkType int networkType) {
        this.networkType = networkType;
        return this;
    }

    public JobIntent setPeriodic(long intervalMillis) {
        this.isPeriodic = true;
        this.intervalMillis = intervalMillis;
        return this;
    }

    public boolean isPeriodic() {
        return isPeriodic;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }

    public boolean isPersisted() {
        return isPersisted;
    }

    public JobIntent setPersisted(boolean persisted) {
        isPersisted = persisted;
        return this;
    }

    @NonNull
    public synchronized Bundle getExtras() {
        if (extras == null) {
            extras = new Bundle();
        }
        return extras;
    }

    public JobIntent setExtras(@Nullable Bundle extras) {
        this.extras = extras;
        return this;
    }

    public synchronized JobIntent putAllExtras(@Nullable Bundle extras) {
        if (extras != null) {
            getExtras().putAll(extras);
        }
        return this;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    public Class<? extends JobService> getJobService() {
        return jobService;
    }

    /**
     * 指定执行Job的Service组件, SDK Level 21及以上使用此方法
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JobIntent setJobService(@NonNull Class<? extends JobService> jobService) {
        this.jobService = jobService;
        return this;
    }

    @Nullable
    public Class<? extends GcmTaskService> getTaskService() {
        return taskService;
    }

    /**
     * 指定执行Job的Service组件, SDK Level 21以前使用此方法
     */
    public JobIntent setTaskService(@NonNull Class<? extends GcmTaskService> taskService) {
        this.taskService = taskService;
        return this;
    }

    public JobIntent validate() {
        if (jobId <= 0) {
            throw new IllegalArgumentException("jobId is invalid");
        }

        if (jobService == null && taskService == null) {
            throw new NullPointerException("jobService or taskService is missing");
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
                .append("{id:").append(jobId)
                .append(",intervalMillis:").append(intervalMillis)
                .append(",isPersisted:").append(isPersisted)
                .append(",net:").append(networkType);

        if (!TextUtils.isEmpty(action)) {
            buf.append(",act:").append(action);
        }

        if (extras != null) {
            buf.append(",extras:").append(extras);
        }

        if (jobService != null) {
            buf.append(",jobSvc:").append(jobService);
        }

        if (taskService != null) {
            buf.append(",taskSvc:").append(taskService);
        }

        return buf.append('}').toString();
    }
}
