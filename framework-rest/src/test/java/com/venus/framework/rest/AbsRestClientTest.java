package com.venus.framework.rest;

import okhttp3.HttpUrl;

import junit.framework.TestCase;

import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by ywu on 15/11/26.
 */
public class AbsRestClientTest extends TestCase {

    /**
     * Test the usage of OkHttp UrlBuilder
     */
    public void testUrlBuilder() throws Exception {
        String base = "https://api.5milesapp.com/api/v2";
        String path = "/init/";
        String query = "?a=h哈";
        String encodedChar = URLEncoder.encode("哈", "utf8");

        URL url = new URL(base + path + query);
        HttpUrl resultUrl = new HttpUrl.Builder()
                .scheme(url.getProtocol())
                .host(url.getHost())
                .encodedPath(url.getPath())
                .query(url.getQuery())
                .addQueryParameter("x", "哈")
                .addQueryParameter("y", "哈")
                .build();

        String expectedUrl = base + path + "?a=h" + encodedChar +
                "&x=" + encodedChar +
                "&y=" + encodedChar;
        assertEquals(expectedUrl, resultUrl.toString());
    }

}
