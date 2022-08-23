package com.venus.framework.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.venus.framework.exception.NetworkException;
import com.venus.framework.exception.RestException;
import com.venus.framework.util.L;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import okhttp3.Response;

import static android.text.TextUtils.isDigitsOnly;
import static com.venus.framework.util.Utils.isEmpty;
import static com.venus.framework.util.Utils.isNotEmpty;

/**
 * Created by ywu on 15/1/21.
 */
public abstract class AbsHttpBodyParser<T> implements HttpBodyParser<T>, HttpConstants {

    @Override
    public boolean isSuccessful(Response resp) {
        return resp.isSuccessful() && isSuccessfulResponseCode(resp);
    }

    private boolean isSuccessfulResponseCode(Response resp) {
        String respCode = resp.header(HEADER_FM_CODE);
        if (isEmpty(respCode)) {
            // 尝试旧的拼写方式
            respCode = resp.header(HEADER_FM_CODE_COMPAT);
        }

        return isEmpty(respCode) || TextUtils.equals(respCode, FM_RESP_CODE_SUCCESS);
    }

    @NonNull
    @Override
    public T parse(@NonNull Response resp, @NonNull String body) throws Exception {
        return parse(body);
    }

    @NonNull
    abstract protected T parse(@NonNull String body) throws Exception;

    /**
     * 检查空白body的情况, 避免出现null.
     *
     * RxJava 2除Maybe外都不允许null, 在此统一处理
     */
    protected String ensureValidBody(@NonNull String body, @NonNull Type type) {
        if (isNotEmpty(body)) return body;

        if (type instanceof Class) {
            Class typeClass = (Class) type;
            // 除非是String, 否则按数组或对象处理
            if (CharSequence.class.isAssignableFrom(typeClass)) {
                return body;
            }
            return Collection.class.isAssignableFrom(typeClass) ? "[]" : "{}";
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class rawType = (Class) parameterizedType.getRawType();
            return Collection.class.isAssignableFrom(rawType) ? "[]" : "{}";
        } else {
            return "{}";
        }
    }

    @Override
    public NetworkException parseError(@NonNull Response resp, @Nullable String body) {
        String contentType = resp.header(HTTP_HEADER_CONTENT_TYPE);
        int errorCode = parseErrorCode(resp);
        String errorMessage;

        if (body == null || isEmpty(body.trim())) {
            errorMessage = getDefaultErrorMessage();
        } else if (contentType != null && contentType.startsWith(HTML_CONTENT_TYPE_PREFIX)) {
            errorMessage = getDefaultErrorMessage();
        } else {
            // 尝试从body解析错误消息
            body = body.trim();
            ErrorMessage err = body.startsWith("{") ? ErrorMessage.parseFromJson(body) : null;

            if (err != null) {
                if (err.getErrorCode() != 0) {
                    errorCode = err.getErrorCode();
                }
                errorMessage = err.getMessage();
            } else {
                errorMessage = body;
            }

            if (isEmpty(errorMessage)) {
                errorMessage = getDefaultErrorMessage();
            }
        }

        return new RestException(resp.code(), errorCode, errorMessage);
    }

    private int parseErrorCode(@NonNull Response resp) {
        int code = FM_ERR_UNKNOWN;
        String strCode = resp.header(HEADER_FM_CODE);
        if (isEmpty(strCode)) {
            // 尝试旧的拼写方式
            strCode = resp.header(HEADER_FM_CODE_COMPAT);
        }

        if (isNotEmpty(strCode) && isDigitsOnly(strCode)) {
            try {
                code = Integer.parseInt(strCode);
            } catch (NumberFormatException e) {
                L.e("parse error code failed", e);
            }
        }

        return code;
    }

    @Override
    public NetworkException parseError(@NonNull IOException e) {
        return new NetworkException(NetworkException.NO_STATUS, getDefaultErrorMessage(), e);
    }

    @Nullable
    protected String getDefaultErrorMessage() {
        return null;  // 此模块不再包含资源, 交由app层处理
    }
}
