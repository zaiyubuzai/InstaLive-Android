package com.venus.framework.rest.signature;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.venus.framework.rest.HttpConstants;
import com.venus.framework.rest.UrlSignature;
import com.venus.framework.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 改写HTTP请求，对请求进行签名. 目前支持URL(HTTP GET)、Form的改写，还原multipart的原始内容非常困难，暂不支持
 * <p>
 * Created by ywu on 2017/2/24.
 */
public class MarsAmazonUrlSignatureInterceptor implements Interceptor, HttpConstants {

    @NonNull
    protected final UrlSignatureConfig config;

    @Inject
    public MarsAmazonUrlSignatureInterceptor(@NonNull UrlSignatureConfig config) {
        this.config = config;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        if (config.isSignatureRequired(req.url())) {
            req = signRequest(req);
        }

        return chain.proceed(req);
    }

    protected long nowMillis() {
        return System.currentTimeMillis();
    }

    @VisibleForTesting
    protected String signUrl(@NonNull String method,
                             @NonNull String url,
                             @Nullable Map<String, ?> params) {
        try {
            return UrlSignature.getInstance().signMarsAmazonUrl(config.getContext(), method, url, params, config.getTracker());
        } catch (Exception e) {
            return null;
        }
    }

    protected Request signRequest(final Request req) {
        final String method = req.method();
        if (HTTP_GET.equalsIgnoreCase(method)) {
            return signHttpGet(req);
        } else if (HTTP_POST.equalsIgnoreCase(method)) {
            return signHttpPost(req);
        } else {
            return req;
        }
    }

    private Request signHttpGet(final Request request) {
        final HttpUrl originUrl = request.url();
        final HttpUrl.Builder urlBuilder = originUrl.newBuilder();

        Map<String, Object> params = new HashMap<>();
        Set<String> paramNames = originUrl.queryParameterNames();
        for (String p : paramNames) {
            // 暂不考虑多值参数
            params.put(p, originUrl.queryParameter(p));
        }

        final long ts = nowMillis();
        params.put(PARAM_TIMESTAMP, ts);
        urlBuilder.setEncodedQueryParameter(PARAM_TIMESTAMP, String.valueOf(ts));

        String signature = signUrl(HTTP_GET, getBaseUrl(originUrl), params);
        if (Utils.isNotEmpty(signature)) {
            urlBuilder.setEncodedQueryParameter(PARAM_SIGN, signature);
        }

        return request.newBuilder()
                .url(urlBuilder.build())
                .build();
    }

    private String getBaseUrl(final HttpUrl url) {
        return url.newBuilder().encodedQuery(null).build().toString();
    }

    private Request signHttpPost(final Request request) {
        final RequestBody originBody = request.body();
        final MediaType originContentType = originBody.contentType();

        if (CONTENT_TYPE_FORM.equals(originContentType)) {
            return signFormBody(request);
        }

//        if (CONTENT_TYPE_MULTIPART_FORM.equals(originContentType)) {
//            return signMultipartBody(request);
//        }

        return request;
    }

    private Request signFormBody(Request request) {
        final FormBody originForm = (FormBody) request.body();
        final FormBody.Builder formBuilder = new FormBody.Builder();
        final Map<String, Object> params = new HashMap<>();

        final int size = originForm.size();
        for (int i = 0; i < size; i++) {
            String key = originForm.name(i);
            String value = originForm.encodedValue(i);
            if (!PARAM_TIMESTAMP.equalsIgnoreCase(key) &&
                    !PARAM_SIGN.equalsIgnoreCase(key)) {
                formBuilder.addEncoded(key, value);
                params.put(key, Utils.decodeUrl(value));
            }
        }

        final long ts = nowMillis();
        params.put(PARAM_TIMESTAMP, ts);
        formBuilder.addEncoded(PARAM_TIMESTAMP, String.valueOf(ts));

        String signature = signUrl(HTTP_POST, getBaseUrl(request.url()), params);
        if (Utils.isNotEmpty(signature)) {
            formBuilder.addEncoded(PARAM_SIGN, signature);
        }

        final FormBody form = formBuilder.build();
        final long contentLength = form.contentLength();
        return request.newBuilder()
                .header("Content-Length", String.valueOf(contentLength))  // 长度发生了变化需改写header
                .post(form)
                .build();
    }

//    private Request signMultipartBody(Request request) {
//        final MultipartBody originBody = (MultipartBody) request.body();
//        final List<Part> originParts = originBody.parts();
//        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
//        final Map<String, Object> params = new HashMap<>();
//
//        for (Part part : originParts) {
//        }
//    }
}
