package com.venus.framework.rest;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import androidx.annotation.NonNull;

import com.venus.framework.util.Utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ywu on 14-9-27.
 */
public class RequestHelper {

    private String rfTag;

    protected final Context context;
    private final HashMap<String, String> headers = new HashMap<String, String>();

    /**
     * 取得应用的User Agent，不含敏感信息
     */
    public static synchronized String getUserAgent(Context context) {
        StringBuilder buf = new StringBuilder();

        PackageInfo pkg = Utils.getPackageInfo(context);
        if (pkg != null) {
            buf.append("5miles ").append(pkg.versionName)
                    .append('(').append(pkg.versionCode).append(')');
        }

        return buf.append(" Android ")
                .append(Build.VERSION.RELEASE)
                .append(' ')
                .append(Utils.encodeUrl(Build.MANUFACTURER))
                .append(' ').append(getModel())
                .toString();
    }

    private static String getModel() {
        return Utils.encodeUrl(Build.MODEL);
    }

    private static String getLanguageCode() {
        return Utils.encodeUrl(Locale.getDefault().getLanguage());
    }

    public RequestHelper(@NonNull Context context) {
        this.context = context;
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    protected void append(String key, Object value) {
        synchronized (headers) {
            headers.put(key, String.valueOf(value));
        }
    }

    protected void append(@NonNull Map<String, String> headers) {
        synchronized (this.headers) {
            this.headers.putAll(headers);
        }
    }

    protected void prepareHeaders() {
    }

    /**
     * 每次请求都会携带的参数
     */
    public RequestParams getParams() {
        return null;
    }

    public String getBaseUrl() {
        return "";
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public Map<String, String> buildHeaders() {
        synchronized (headers) {
            // 目前每次重新构建header，避免注销后无法清除session信息
            headers.clear();
            prepareHeaders();
            return (Map<String, String>) headers.clone();
        }
    }

    /**
     * 是否使能url签名
     */
    public boolean isUrlSignatureEnabled() {
        return false;
    }

    /**
     * 记录一条诊断信息,目前用来帮助查找签名的问题
     */
    public void logException(Exception ex) {
    }

    /**
     * 如果activityHashCode不为null ,优先去activity取
     */
    public String getRfTag() {
        return rfTag;
    }

    public void setRfTag(String rfTag) {
        this.rfTag = rfTag;
    }

    public Integer getActivityHashCode() {
        return null;
    }

    @NonNull
    public String getCloudImagePrefix() {
        return "";
    }
}
