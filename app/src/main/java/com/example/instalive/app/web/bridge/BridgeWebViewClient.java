package com.example.instalive.app.web.bridge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.instalive.InstaLiveApp;
import com.example.instalive.utils.ShareUtility;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import timber.log.Timber;

/**
 * Created by bruce on 10/28/15.
 */
public class BridgeWebViewClient extends WebViewClient {
    protected BridgeWebView bridgeWebView;

    public BridgeWebViewClient(BridgeWebView bridgeWebView) {
        this.bridgeWebView = bridgeWebView;
    }

    @Override
    public final boolean shouldOverrideUrlLoading(WebView view, String url) {
        Timber.d(url);
        if (url.startsWith(BridgeUtil.YY_RETURN_DATA)) { // 如果是返回数据
            try {
                String decodedUrl = URLDecoder.decode(url, "UTF-8");
                bridgeWebView.handlerReturnData(decodedUrl);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true;
        } else if (url.startsWith(BridgeUtil.YY_OVERRIDE_SCHEMA)) { //
            bridgeWebView.flushMessageQueue();
            return true;
        } else if (url.startsWith(BridgeUtil.MAILTO_OVERRIDE_SCHEMA)) { //
            bridgeWebView.flushMessageQueue();
            ShareUtility.INSTANCE.shareEmail(InstaLiveApp.Companion.getAppInstance().getActivityList().get(0), url);
            return true;
        }

        return overrideUrlLoading(view, url) || super.shouldOverrideUrlLoading(view, url);
    }

    protected boolean overrideUrlLoading(WebView view, String url) {
        return false;
    }

    @Override
    public final void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (BridgeWebView.toLoadJs != null) {
            BridgeUtil.webViewLoadLocalJs(view, BridgeWebView.toLoadJs);
        }

        //
        if (bridgeWebView.getStartupMessage() != null) {
            for (Message m : bridgeWebView.getStartupMessage()) {
                bridgeWebView.dispatchMessage(m);
            }
            bridgeWebView.setStartupMessage(null);
        }

        doOnPageFinished(view, url);
    }

    protected void doOnPageFinished(WebView view, String url) {
    }
}
