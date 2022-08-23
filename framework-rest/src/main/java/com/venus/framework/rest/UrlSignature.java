package com.venus.framework.rest;

import android.content.Context;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.soloader.SoLoader;
import com.venus.framework.Constants;
import com.venus.framework.util.L;
import com.venus.framework.util.Tracker;
import com.venus.framework.util.jni.NativeLoader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kotlin.jvm.functions.Function5;

/**
 * Signature utilities for API URLs
 * Created by ywu on 15/12/28.
 */
public final class UrlSignature implements Constants {

    private static Future<UrlSignature> signatureFutureTask;

    private static ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    /**
     * 异步加载so
     * 只需要调用一次，需要在调用处控制
     * @param context Context
     */
    public static void initAsync(@NonNull final Context context) {
        signatureFutureTask = singleThreadExecutor.submit(() -> {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            return new UrlSignature(context);
        });
    }

    public static Future<UrlSignature> getFutureInstance() {
        if (signatureFutureTask == null) {
            throw new RuntimeException("please call UrlSignature initAsync");
        }

        return signatureFutureTask;
    }

    /**
     * 如果so没有加载完成，会阻塞当前线程
     *
     * @return UrlSignature
     */
    public static UrlSignature getInstance() {
        if (signatureFutureTask == null) {
            throw new RuntimeException("please call initAsync or init method");
        }

        try {
            return signatureFutureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("UrlSignature Async initialization failed");
        }
    }

    private UrlSignature(@NonNull final Context context) {
        final String lib = "fmrest";
        try {
            SoLoader.init(context, false);
            SoLoader.loadLibrary(lib);
        } catch (UnsatisfiedLinkError error) {
            NativeLoader.loadLibrary(context, lib);
        }
    }

    /**
     * 给定url请求信息,给出对应的数字签名
     *
     * @param context Context
     * @param method HTTP method
     * @param url    url,可包含query
     * @param params 请求参数
     * @param tracker 暂时增加一个RequestHelper用来记录一些debug信息
     * @return 签名
     * @see <a href="https://github.com/3rdStone/fivemiles-auction/wiki/%E7%AD%BE%E5%90%8D%E6%96%B9%E6%B3%95">5miles URL签名文档</a>
     * @see <a href="https://dev.twitter.com/oauth/overview/creating-signatures">Twitter文档参考</a>
     */
    @Nullable
    public String signUrl(@NonNull Context context,
                          @NonNull String method,
                          @NonNull String url,
                          @Nullable Map<String, ?> params,
                          @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signUrl, tracker);
    }

    @Nullable
    public String signMarsUrl(@NonNull Context context,
                          @NonNull String method,
                          @NonNull String url,
                          @Nullable Map<String, ?> params,
                          @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signMarsUrl, tracker);
    }

    @Nullable
    public String signMarsPlayUrl(@NonNull Context context,
                              @NonNull String method,
                              @NonNull String url,
                              @Nullable Map<String, ?> params,
                              @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signMarsPlayUrl, tracker);
    }

    @Nullable
    public String signMarsAmazonUrl(@NonNull Context context,
                                  @NonNull String method,
                                  @NonNull String url,
                                  @Nullable Map<String, ?> params,
                                  @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signMarsAmazonUrl, tracker);
    }

    @Nullable
    public String signVenusUrl(@NonNull Context context,
                              @NonNull String method,
                              @NonNull String url,
                              @Nullable Map<String, ?> params,
                              @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signVenusUrl, tracker);
    }

    @Nullable
    public String signVenusPlayUrl(@NonNull Context context,
                               @NonNull String method,
                               @NonNull String url,
                               @Nullable Map<String, ?> params,
                               @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signVenusPlayUrl, tracker);
    }

    /**
     * 给定url请求信息,给出对应的数字签名
     *
     * @param context Context
     * @param method HTTP method
     * @param url    base url, 不包含query
     * @param data   请求参数字典
     * @return 签名
     * @see <a href="https://github.com/3rdStone/fivemiles-auction/wiki/%E7%AD%BE%E5%90%8D%E6%96%B9%E6%B3%95">5miles URL签名文档</a>
     * @see <a href="https://dev.twitter.com/oauth/overview/creating-signatures">Twitter文档参考</a>
     */
    @Nullable
    public String signWebUrl(@NonNull Context context,
                             @NonNull String method,
                             @NonNull String url,
                             @Nullable JSONObject data) {
        Map<String, String> paramMap = new HashMap<>();
        if (data != null) {
            Iterator<String> itKeys = data.keys();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                paramMap.put(key, data.optString(key));
            }
        }
        return signUrl(context, method, url, paramMap, UrlSignature::signWebUrl, null);
    }

    @Nullable
    public String signMarsWebUrl(@NonNull Context context,
                             @NonNull String method,
                             @NonNull String url,
                             @Nullable JSONObject data) {
        Map<String, String> paramMap = new HashMap<>();
        if (data != null) {
            Iterator<String> itKeys = data.keys();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                paramMap.put(key, data.optString(key));
            }
        }
        return signUrl(context, method, url, paramMap, UrlSignature::signMarsWebUrl, null);
    }

    @Nullable
    public String signMarsWebPlayUrl(@NonNull Context context,
                                 @NonNull String method,
                                 @NonNull String url,
                                 @Nullable JSONObject data) {
        Map<String, String> paramMap = new HashMap<>();
        if (data != null) {
            Iterator<String> itKeys = data.keys();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                paramMap.put(key, data.optString(key));
            }
        }
        return signUrl(context, method, url, paramMap, UrlSignature::signMarsWebPlayUrl, null);
    }

    @Nullable
    public String signVenusWebUrl(@NonNull Context context,
                                 @NonNull String method,
                                 @NonNull String url,
                                 @Nullable JSONObject data) {
        Map<String, String> paramMap = new HashMap<>();
        if (data != null) {
            Iterator<String> itKeys = data.keys();
            while (itKeys.hasNext()) {
                String key = itKeys.next();
                paramMap.put(key, data.optString(key));
            }
        }
        return signUrl(context, method, url, paramMap, UrlSignature::signVenusWebUrl, null);
    }

    /**
     * 给定url请求信息,给出对应的数字签名
     *
     * @param context Context
     * @param method HTTP method
     * @param url url,可包含query
     * @param params 请求参数
     * @return 签名
     * @see <a href="https://github.com/3rdStone/fivemiles-auction/wiki/%E7%AD%BE%E5%90%8D%E6%96%B9%E6%B3%95">5miles URL签名文档</a>
     * @see <a href="https://dev.twitter.com/oauth/overview/creating-signatures">Twitter文档参考</a>
     */
    @Nullable
    private String signUrl(@NonNull Context context,
                           @NonNull String method,
                           @NonNull String url,
                           @Nullable Map<String, ?> params,
                           @NonNull Function5<Context, String, String, String[], byte[][], String> func,
                           @Nullable Tracker tracker) {
        Map<String, String> paramMap = new HashMap<>();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (k != null && v != null) {
                    // 此处排除值为null的参数,与AbsRestClient的处理一致
                    paramMap.put(k, String.valueOf(v));
                }
            }
        }

        // 提取url包含的query参数
        String baseUrl = url;
        int idxQuery = url.indexOf('?');
        if (idxQuery >= 0 && idxQuery < url.length() - 1) {
            baseUrl = url.substring(0, idxQuery);
            String query = url.substring(idxQuery + 1);

            // 此处不使用Uri类解析query string,避免出现isn't a hierarchical URI的问题
            String[] kvList = query.split("&");
            for (String kv : kvList) {
                String[] pair = kv.split("=", 2);
                String k = pair[0], v = (pair.length > 1) ? pair[1] : "";
                paramMap.put(k, v);  // url中包含的参数,一定要算在签名里,否则就不一致了
            }
        }


        try {
            // 转换为简单数据类型,减少native层无谓的性能开销
            String[] paramNames = new String[paramMap.size()];
            byte[][] paramValues = new byte[paramMap.size()][];
            int i = 0;
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                paramNames[i] = entry.getKey();
                paramValues[i] = entry.getValue().getBytes(UTF_8);
                i++;
            }

            String signature = func.invoke(context, method, baseUrl, paramNames, paramValues);

            // 记录一些debug信息,帮助查找签名错误的原因
            L.v("sign %s|%s|%s|%s", method, baseUrl, paramMap, signature);
            if (tracker != null) {
                if (paramMap.containsKey("password")) paramMap.put("password", "*");  // 隐去敏感信息
                tracker.log("sign %s|%s|%s|%s", method, baseUrl, paramMap, signature);
            }

            return signature;
        } catch (Exception e) {
            L.e("sign url failed", e);
            return null;
        }
    }

    /**
     * Collect sdk 签名
     *
     * @param context Context
     * @param data 请求签名的字符串
     * @return 签名
     * @see <a href="https://5milesrd.quip.com/bcaQA6KBu3j7">5miles URL签名文档</a>
     */
    public String signCollectUrl(@NonNull Context context, @NonNull String data) {
        return signCollectUrl(context, data.getBytes(UTF_8));
    }

    /**
     * cmt cube 签名
     *
     * @param context
     * @param data
     * @return
     */
    public String signCubeUrl(@NonNull Context context,
                              @NonNull String method,
                              @NonNull String url,
                              @NonNull String data) {
        return signCubeUrl(context, method, url, data.getBytes(UTF_8));
    }

    /**
     * cmt wallet 签名
     *
     * @param context
     * @param data
     * @return
     */
    public String signCmtWalletUrl(@NonNull Context context,
                                   @NonNull String method,
                                   @NonNull String url,
                                   @NonNull String data) {
        return signCmtWalletUrl(context, method, url, data.getBytes(UTF_8));
    }

    /**
     * URL signature for Crypto Wallet (Noomi)
     */
    public String signCryptoWalletUrl(@NonNull Context context,
                                      @NonNull String method,
                                      @NonNull String url,
                                      @Nullable Map<String, ?> params,
                                      @Nullable Tracker tracker) {
        return signUrl(context, method, url, params, UrlSignature::signCryptoWalletUrl, tracker);
    }

    /**
     * URL signature for Crypto Wallet (Noomi)
     */
    public String signCryptoWalletUrl(@NonNull Context context,
                                      @NonNull String method,
                                      @NonNull String url,
                                      @NonNull String data) {
        return signCryptoWalletBody(context, method, url, data.getBytes(UTF_8));
    }

    /**
     * 给定url请求信息,给出对应的数字签名
     * @param method HTTP method
     * @param baseUrl url的主干部分(scheme/host/path),不包含query
     * @param paramNames 请求参数的key列表
     * @param paramValues 请求参数的value列表
     * @return 签名
     */
    private static synchronized native String signUrl(@NonNull Context context,
                                                      @NonNull String method,
                                                      @NonNull String baseUrl,
                                                      @NonNull String[] paramNames,
                                                      @NonNull byte[][] paramValues);

    private static synchronized native String signMarsUrl(@NonNull Context context,
                                                      @NonNull String method,
                                                      @NonNull String baseUrl,
                                                      @NonNull String[] paramNames,
                                                      @NonNull byte[][] paramValues);

    private static synchronized native String signMarsPlayUrl(@NonNull Context context,
                                                          @NonNull String method,
                                                          @NonNull String baseUrl,
                                                          @NonNull String[] paramNames,
                                                          @NonNull byte[][] paramValues);

    private static synchronized native String signMarsAmazonUrl(@NonNull Context context,
                                                              @NonNull String method,
                                                              @NonNull String baseUrl,
                                                              @NonNull String[] paramNames,
                                                              @NonNull byte[][] paramValues);


    private static synchronized native String signVenusUrl(@NonNull Context context,
                                                          @NonNull String method,
                                                          @NonNull String baseUrl,
                                                          @NonNull String[] paramNames,
                                                          @NonNull byte[][] paramValues);


    private static synchronized native String signVenusPlayUrl(@NonNull Context context,
                                                           @NonNull String method,
                                                           @NonNull String baseUrl,
                                                           @NonNull String[] paramNames,
                                                           @NonNull byte[][] paramValues);

    /**
     * 给定url请求信息,给出对应的数字签名
     *
     * @param method      HTTP method
     * @param baseUrl     url的主干部分(scheme/host/path),不包含query
     * @param paramNames  请求参数的key列表
     * @param paramValues 请求参数的value列表
     * @return 签名
     */
    private static synchronized native String signWebUrl(@NonNull Context context,
                                                         @NonNull String method,
                                                         @NonNull String baseUrl,
                                                         @NonNull String[] paramNames,
                                                         @NonNull byte[][] paramValues);

    private static synchronized native String signMarsWebUrl(@NonNull Context context,
                                                         @NonNull String method,
                                                         @NonNull String baseUrl,
                                                         @NonNull String[] paramNames,
                                                         @NonNull byte[][] paramValues);

    private static synchronized native String signMarsWebPlayUrl(@NonNull Context context,
                                                             @NonNull String method,
                                                             @NonNull String baseUrl,
                                                             @NonNull String[] paramNames,
                                                             @NonNull byte[][] paramValues);

    private static synchronized native String signVenusWebUrl(@NonNull Context context,
                                                             @NonNull String method,
                                                             @NonNull String baseUrl,
                                                             @NonNull String[] paramNames,
                                                             @NonNull byte[][] paramValues);

    /**
     * Collect sdk 签名
     *
     * @param data 请求参数
     * @return 签名
     */
    private static synchronized native String signCollectUrl(@NonNull Context context,
                                                             @NonNull byte[] data);

    /**
     * cmt cube 签名
     *
     * @param context
     * @param data
     * @return
     */
    private static synchronized native String signCubeUrl(@NonNull Context context,
                                                          @NonNull String method,
                                                          @NonNull String url,
                                                          @NonNull byte[] data);

    /**
     * URL signature for Crypto Wallet (Noomi)
     */
    private static synchronized native String signCryptoWalletUrl(@NonNull Context context,
                                                                  @NonNull String method,
                                                                  @NonNull String baseUrl,
                                                                  @NonNull String[] paramNames,
                                                                  @NonNull byte[][] paramValues);

    /**
     * URL signature for Crypto Wallet (Noomi)
     */
    private static synchronized native String signCryptoWalletBody(@NonNull Context context,
                                                                   @NonNull String method,
                                                                   @NonNull String url,
                                                                   @NonNull byte[] data);

    /**
     * cmt wallet 签名
     *
     * @param context
     * @param data
     * @return
     */
    private static synchronized native String signCmtWalletUrl(@NonNull Context context,
                                                          @NonNull String method,
                                                          @NonNull String url,
                                                          @NonNull byte[] data);
}