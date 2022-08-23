package com.venus.framework.rest.signature;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.venus.framework.util.L;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.venus.framework.util.Utils.isEmpty;
import static com.venus.framework.util.Utils.isNotEmpty;

/**
 * 包含Server对时逻辑的URL签名拦截器
 * <p>
 * Created by ywu on 2017/2/25.
 */
public class UrlSignatureWithServerTimeInterceptor extends UrlSignatureInterceptor {
    private static final Pattern PATTERN_SERVER_TIME_RESP = Pattern.compile("^\\{\"ts\":\\s*(\\d+).*");
    private static final String SERVER_TIME_API_PATH = "sync_timestamp/";

    /**
     * Server time和local time的偏差值, diff = serverTime - localTime
     */
    private static final AtomicLong serverTimeDiff = new AtomicLong(0);

    /**
     * 用于获取server时间的http client
     */
    @NonNull
    private final OkHttpClient httpClient;

    @Inject
    public UrlSignatureWithServerTimeInterceptor(@NonNull UrlSignatureConfig config,
                                                 @NonNull OkHttpClient httpClient) {
        super(config);
        this.httpClient = httpClient;
    }

    /**
     * 重写获取当前时间戳的方法，返回经过校正的时间戳
     */
    @Override
    protected long nowMillis() {
        return System.currentTimeMillis() + serverTimeDiff.get();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        long currTimeDiff = serverTimeDiff.get();  // 记录发送请求前的时间差
        Response originResponse = super.intercept(chain);
        if (originResponse.isSuccessful() || !isInvalidClientTimeError(originResponse)) {
            return originResponse;
        }

        // 校正时间，重新签名后再发送
        final Request originRequest = chain.request();
        final boolean diffFetched = syncServerTime(originRequest, currTimeDiff);  // 如果做了校正仍然报错，说明需要重新同步
        if (diffFetched) {
            Request signedRequest = signRequest(originRequest);
            return chain.proceed(signedRequest);
        }

        return originResponse;
    }

    public boolean isInvalidClientTimeError(Response resp) {
        String strCode = resp.header(HEADER_FM_CODE);
        if (isEmpty(strCode)) {
            strCode = resp.header(HEADER_FM_CODE_COMPAT);
        }

        return isNotEmpty(strCode) && TextUtils.equals(strCode, FM_RESP_CODE_INVALID_CLIENT_TIME);
    }

    private boolean syncServerTime(Request originRequest, long failedTimeDiff) {
        if (serverTimeDiff.get() != 0 && failedTimeDiff == 0) {
            // 如果之前没有校正，使用此时的校正值即可
            return true;
        }

        synchronized (serverTimeDiff) {
            // 获得锁后再次检查
            long currDiff = serverTimeDiff.get();
            return currDiff != failedTimeDiff ||      // 校正值已更新（由其他并发线程）
                    doSyncServerTime(originRequest);  // 尝试校正
        }
    }

    private boolean doSyncServerTime(Request originRequest) {
        Request req = new Request.Builder()
                .url(getServerTimeUrl())
                .headers(originRequest.headers())
                .removeHeader("Accept-Encoding")  // 去掉原请求的gzip头，从而使用transparent gzip，否则需要自己解压
                .build();

        ResponseBody body = null;
        boolean fetched = false;
        try {
            L.v("http %s <%s>", req.method(), req.url());
            Response resp = httpClient.newCall(req).execute();
            body = resp.body();
            L.v("http response: [%d]", resp.code());
            if (resp.isSuccessful()) {
                fetched = onServerTimeUpdated(body.string());
            }
        } catch (Exception e) {
            L.e("request failed [%s] %s: %s", req.method(), req.url(), e);
        } finally {
            if (body != null) {
                body.close();
            }
        }
        return fetched;
    }

    private boolean onServerTimeUpdated(String data) {
        boolean handled = false;
        L.v("client-server time calibration result: %s", data);

        Matcher m = PATTERN_SERVER_TIME_RESP.matcher(data);
        if (m.matches()) {
            String strMillis = m.group(1);
            long tsMillis = Long.parseLong(strMillis);
            serverTimeDiff.set(tsMillis - System.currentTimeMillis());
            handled = true;
        }

        return handled;
    }

    protected HttpUrl getServerTimeUrl() {
        return HttpUrl.parse(config.getApiBaseUrl())
                .newBuilder()
                .addEncodedPathSegments(SERVER_TIME_API_PATH)
                .build();
    }
}
