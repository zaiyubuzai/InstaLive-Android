package com.venus.framework.util.jobs;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Network type definitions for Job scheduling
 * <p/>
 * Created by ywu on 16/9/6.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        JobConstants.NETWORK_TYPE_UNSPECIFIED,
        JobConstants.NETWORK_TYPE_CONNECTED,
        JobConstants.NETWORK_TYPE_UNMETERED,
})
public @interface NetworkType {
}
