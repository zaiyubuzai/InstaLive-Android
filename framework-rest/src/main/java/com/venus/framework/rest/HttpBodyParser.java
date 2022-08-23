package com.venus.framework.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.venus.framework.exception.NetworkException;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by ywu on 14-9-27.
 */
public interface HttpBodyParser<T> {

    boolean isSuccessful(Response resp);

    @NonNull
    T parse(@NonNull Response resp, @NonNull String body) throws Exception;

    NetworkException parseError(@NonNull Response resp, @Nullable String body);

    NetworkException parseError(@NonNull IOException e);
}
