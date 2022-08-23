/*
 * OkHttp Call utilities.
 *
 * Created by ywu on 2017/10/1.
 */
package com.venus.framework.rest.util

import okhttp3.Call
import okhttp3.OkHttpClient

/**
 * Cancel OkHttp calls by tag
 */
fun OkHttpClient.cancelTag(tag: Any) {
    dispatcher().queuedCalls()
            .filter { it.request().tag() == tag }
            .forEach(Call::cancel)
    dispatcher().runningCalls()
            .filter { it.request().tag() == tag }
            .forEach(Call::cancel)
}
