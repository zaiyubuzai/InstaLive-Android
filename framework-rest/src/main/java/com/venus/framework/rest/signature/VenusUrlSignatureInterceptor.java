package com.venus.framework.rest.signature;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.venus.framework.rest.HttpConstants;
import com.venus.framework.rest.UrlSignature;
import com.venus.framework.util.Utils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * 改写HTTP请求，对请求进行签名. 目前支持URL(HTTP GET)、Form的改写，还原multipart的原始内容非常困难，暂不支持
 * <p>
 * Created by ywu on 2017/2/24.
 */
public class VenusUrlSignatureInterceptor implements Interceptor, HttpConstants {

    @NonNull
    protected final UrlSignatureConfig config;

    @Inject
    public VenusUrlSignatureInterceptor(@NonNull UrlSignatureConfig config) {
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
        return UrlSignature.getInstance().signVenusUrl(config.getContext(), method, url, params, config.getTracker());
    }

    protected Request signRequest(final Request req) throws IOException {
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

    private Request signHttpPost(final Request request) throws IOException {
        final RequestBody originBody = request.body();
        final MediaType originContentType = originBody.contentType();

        if (CONTENT_TYPE_FORM.equals(originContentType)) {
            return signFormBody(request);
//        } else if (CONTENT_TYPE_MULTIPART_FORM.equals(originContentType)) {
//            return signMultipartBody(request);
        } else if (originContentType == null){
            return signNullBody(request);//logout接口会触发
        }

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

    private Request signMultipartBody(Request request) {
        final MultipartBody originBody = (MultipartBody) request.body();
        final List<MultipartBody.Part> originParts = originBody.parts();
        final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        final Map<String, Object> params = new HashMap<>();

        for (MultipartBody.Part part : originParts) {
            bodyBuilder.addPart(part);
            MediaType mediaType = part.body().contentType();
            if (mediaType == null) {
                String normalParamKey;
                String normalParamValue;
                try {
                    normalParamValue = getParamContent(part.body());
                    Headers headers = part.headers();
                    if (!TextUtils.isEmpty(normalParamValue) && headers != null) {
                        for (String name : headers.names()) {
                            String headerContent = headers.get(name);
                            if (!TextUtils.isEmpty(headerContent)) {
                                String[] normalParamKeyContainer = headerContent.split("name=\"");
                                if (normalParamKeyContainer.length == 2) {
                                    normalParamKey = normalParamKeyContainer[1].split("\"")[0];
                                    params.put(normalParamKey, normalParamValue);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // ToDo 此处可对参数做签名处理
        final long ts = nowMillis();
        params.put(PARAM_TIMESTAMP, ts);
        bodyBuilder.addPart(MultipartBody.Part.createFormData(PARAM_TIMESTAMP, String.valueOf(ts)));

        String signature = signUrl(HTTP_POST, getBaseUrl(request.url()), params);
        if (Utils.isNotEmpty(signature)) {
            bodyBuilder.addPart(MultipartBody.Part.createFormData(PARAM_SIGN, signature));
        }
        /**
         * String sign = SignUtil.sign(signParams);
         * jsonObject.put("sign", sign);
         */
        MultipartBody newRequestBody = bodyBuilder.build();
        long contentLength = 1000;
        try {
            contentLength = newRequestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request.newBuilder()
                .header("Content-Length", String.valueOf(contentLength))  // 长度发生了变化需改写header
                .post(newRequestBody)
                .build();
    }

    private Request signJsonBody(Request request) {
        RequestBody requestBody = request.body();
        RequestBody newRequestBody;
        final Map<String, Object> params = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        try {
            assert requestBody != null;
            if (requestBody.contentLength() != 0) {
                jsonObject = new JSONObject(getParamContent(requestBody));
            }

            // ToDo 此处可对参数做签名处理
            final long ts = nowMillis();
            params.put(PARAM_TIMESTAMP, ts);
            jsonObject.put(PARAM_TIMESTAMP, String.valueOf(ts));

            String signature = signUrl(HTTP_POST, getBaseUrl(request.url()), params);
            if (Utils.isNotEmpty(signature)) {
                jsonObject.put(PARAM_SIGN, signature);
            }
            /**
             * String sign = SignUtil.sign(signParams);
             * jsonObject.put("sign", sign);
             */
            newRequestBody = RequestBody.create(requestBody.contentType(), jsonObject.toString());

        } catch (Exception e) {
            newRequestBody = requestBody;
            e.printStackTrace();
        }
        final long contentLength = jsonObject.length();
        return request.newBuilder()
                .header("Content-Length", String.valueOf(contentLength))  // 长度发生了变化需改写header
                .post(newRequestBody)
                .build();
    }

    private Request signNullBody(Request request) {
        RequestBody requestBody = request.body();
        assert requestBody != null;

        RequestBody newRequestBody;
        final Map<String, Object> params = new HashMap<>();
        StringBuilder stringBuffer = new StringBuilder();
        try {
            // ToDo 此处可对参数做签名处理
            final long ts = nowMillis();
            params.put(PARAM_TIMESTAMP, ts);
            stringBuffer.append("ts=");
            stringBuffer.append(ts);

            String signature = signUrl(HTTP_POST, getBaseUrl(request.url()), params);
            if (Utils.isNotEmpty(signature)) {
                stringBuffer.append("&");
                stringBuffer.append("sign=");
                stringBuffer.append(signature);
            }
            /**
             * String sign = SignUtil.sign(signParams);
             * jsonObject.put("sign", sign);
             */
            newRequestBody = RequestBody.create(MediaType.parse("text/html"), stringBuffer.toString());

        } catch (Exception e) {
            newRequestBody = requestBody;
            e.printStackTrace();
        }
        final long contentLength = stringBuffer.length();
        return request.newBuilder()
                .header("Content-Length", String.valueOf(contentLength))  // 长度发生了变化需改写header
                .post(newRequestBody)
                .build();
    }

    /**
     * 获取常规post请求参数
     */
    private String getParamContent(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }
}
