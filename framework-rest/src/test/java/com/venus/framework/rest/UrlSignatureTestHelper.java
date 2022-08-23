package com.venus.framework.rest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Base64;

import com.venus.framework.rest.HttpConstants;
import com.venus.framework.rest.RequestParams;

import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Helper to generate URL signature
 * <p/>
 * <p/>
 * NOTE 此处包含URL签名方法的Java实现,目的是方便调试,此处任何逻辑及常量定义必须与产品代码保持隔离(即不参与APK打包),以免泄露算法或秘钥
 * <p/>
 * Created by ywu on 16/6/27.
 */
public class UrlSignatureTestHelper {

    static final String API_URL_PREFIX = "https://api-test.5milesapp.com/api/v2";  // api url prefix
    static final String SECRET = "&Ad$iOI34HNlK";  // secret following an '&'
    static final String KEY = "Ad$iOI34HNlK";
    static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    @Test
    public void testSignature() throws Exception {
        long ts = 0;
        String sign = "";
        String url = "";
        Map<String, Object> params = new HashMap<>();

        ts = System.currentTimeMillis();
        url = API_URL_PREFIX + "/home_for_sale/";
        params.clear();
        params.put("ts", ts);
        params.put("lon", -96.77);
        params.put("lat", 32.82);

        sign = signUrl(HttpConstants.HTTP_GET, url, params);

        System.out.printf("timestamp:\t%d\nsignature:\t%s\n", ts, sign);
        System.out.printf("signed url:\t%s%s%s&sign=%s\n\n",
                url, (url.contains("?") ? '&' : '?'), toQuery(params), sign);
    }

    @Test
    public void testSignatureWithEmoji() throws Exception {
        long ts = 0;
        String sign = "";
        String url = "";
        Map<String, Object> params = new HashMap<>();

        ts = 1468902936598L;
        url = API_URL_PREFIX + "/make_offer/";
        params.clear();
        params.put("text", "\uD83D\uDE00");
        params.put("item_id", "Y2ZBMP16mDgn3kzj");
        params.put("ts", ts);
        params.put("to_user", "dKzoVqOJPD");
        params.put("msg_type", 0);

        sign = signUrl(HttpConstants.HTTP_POST, url, params);
        Assert.assertEquals("9765a4e91a90ea9ea6e41964abcee8e8e494d90a", sign);

        System.out.printf("timestamp:\t%d\nsignature:\t%s\n", ts, sign);
        System.out.printf("signed url:\t%s%s%s&sign=%s\n\n",
                url, (url.contains("?") ? '&' : '?'), toQuery(params), sign);
    }

    @Test
    public void testSignUrlWithEscapedChar() throws Exception {
        String url = "https://api.5milesapp.com/api/v2/search_new/";
        RequestParams params = new RequestParams();
        String sign;
        long ts = 1471963619519L;

        // ?q=children&#x27;s courtyard&lon=-97.0100021&lat=32.6899986&refind=0&ts=1471963619519&rf_tag=P685&sign=53471035f4d07c79e53b32bb5eb17aeb7f0c4041
        // encoded q=children%26%23x27;s%20courtyard
        // should be: children's courtyard
        params.clear();
        params.put("q", "children&#x27;s courtyard")
                .put("rf_tag", "P685")
                .put("lon", "-97.0100021")
                .put("lat", "32.6899986")
                .put("ts", ts);
        sign = signUrl(HttpConstants.HTTP_GET, url, params);

        System.out.printf("timestamp:\t%d\nsignature:\t%s\n", ts, sign);
        System.out.printf("signed url:\t%s%s%s&sign=%s\n\n",
                url, (url.contains("?") ? '&' : '?'), toQuery(params), sign);
    }

    /**
     * 给定url请求信息,给出对应的数字签名
     *
     * @param method HTTP method
     * @param url    url,可包含query
     * @param params 请求参数
     * @return 签名
     * @see <a href="https://github.com/3rdStone/fivemiles-auction/wiki/%E7%AD%BE%E5%90%8D%E6%96%B9%E6%B3%95">5miles URL签名文档</a>
     * @see <a href="https://dev.twitter.com/oauth/overview/creating-signatures">Twitter文档参考</a>
     */
    @Nullable
    public static String signUrl(@NonNull String method,
                                 @NonNull String url,
                                 @Nullable Map<String, ?> params) throws Exception {
        ArrayList<String[]> paramList = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, ?> entry : params.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();
                if (k != null && v != null) {
                    // 此处排除值为null的参数,与AbsRestClient的处理一致
                    paramList.add(new String[]{k, String.valueOf(v)});
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
                String[] pair = kv.split("=");
                String k = pair[0], v = (pair.length > 1) ? pair[1] : "";
                paramList.add(new String[]{k, v});  // url中包含的参数,一定要算在签名里,否则就不一致了
            }
        }

        return signUrl(method, baseUrl, paramList);
    }

    private static String signUrl(@NonNull String method,
                                  @NonNull String baseUrl,
                                  @NonNull List<String[]> params) throws Exception {
        Collections.sort(params, new Comparator<String[]>() {
            @Override
            public int compare(String[] lhs, String[] rhs) {
                if (lhs[0].equals(rhs[0])) {
                    // key相等则按value排序
                    return lhs[1].compareTo(rhs[1]);
                }

                return lhs[0].compareTo(rhs[0]);
            }
        });

        StringBuilder paramsBuf = new StringBuilder();
        for (String[] p : params) {
            if (paramsBuf.length() > 0) {
                paramsBuf.append('&');
            }
            paramsBuf.append(p[0]).append('=').append(p[1]);
        }

        String msg = method + '&' + baseUrl + '&' + paramsBuf + SECRET;
        System.out.println("message to sign: " + msg);
        return sign(msg);
    }

    private static String toQuery(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (buf.length() > 0) {
                buf.append('&');
            }

            String k = entry.getKey();
            Object v = entry.getValue();
            if (k != null && v != null) {
                buf.append(k).append('=').append(String.valueOf(v));
            }
        }
        return buf.toString();
    }

    private static String sign(String msg) throws Exception {
        return calcHmacSha1Hex(msg);
    }

    private static String calcHmacSha1Hex(String msg) throws Exception {
        return toHexString(calcHmacSha1(msg));
    }

    private static String calcHmacSha1Base64(String msg) throws Exception {
        byte[] rawHmac = calcHmacSha1(msg);
        return new String(Base64.encode(rawHmac, Base64.DEFAULT));
    }

    private static byte[] calcHmacSha1(String msg) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(KEY.getBytes(), HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);

        return mac.doFinal(msg.getBytes());
    }

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();

        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }
}
