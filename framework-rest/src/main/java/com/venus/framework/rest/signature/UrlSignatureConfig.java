package com.venus.framework.rest.signature;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.venus.framework.util.Tracker;

import okhttp3.HttpUrl;

/**
 * Created by ywu on 2017/2/27.
 */

public interface UrlSignatureConfig {

    @NonNull Context getContext();

    boolean isSignatureRequired(HttpUrl url);

    String getApiBaseUrl();

    @Nullable Tracker getTracker();
}
