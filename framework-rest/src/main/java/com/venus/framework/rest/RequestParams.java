package com.venus.framework.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils for the convenience of passing request params
 * <p>
 * Created by ywu on 15/11/26.
 */
public class RequestParams extends HashMap<String, Object> {

    public RequestParams() {
    }

    public RequestParams(@NonNull Map<String, ?> params) {
        putAll(params);
    }

    public RequestParams(@Nullable Object... values) {
        if (values != null) {
            putAll(asMap(values));
        }
    }

    @NonNull
    @Override
    public RequestParams put(String name, Object value) {
        super.put(name, value);
        return this;
    }

    protected static Map<String, ?> asMap(@NonNull Object... values) {
        if (values.length % 2 != 0) {
            throw new RuntimeException("Usage - (key, value, key, value, ...)");
        } else {
            Map<String, Object> result = new HashMap<>(values.length / 2);

            for (int i = 0; i < values.length; i += 2) {
                result.put(String.valueOf(values[i]), values[i + 1]);
            }

            return result;
        }
    }
}
