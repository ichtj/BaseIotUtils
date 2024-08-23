package com.ichtj.basetools.webviews;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.face_chtj.base_iotutils.NetUtils;
import com.face_chtj.base_iotutils.ShellUtils;
import com.face_chtj.base_iotutils.TimeUtils;
import com.ichtj.basetools.R;
import com.ichtj.basetools.base.BaseActivity;
import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;

import java.net.MalformedURLException;
import java.net.URL;

public class WebViewAty extends Activity {
    private static final String TAG = WebViewAty.class.getSimpleName();
    private WebView webView;
    String url = "https://sp.spx.uat.shopee.sg/selfservice/locker";
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        webView = (WebView) findViewById(R.id.webview);
        Log.d(TAG, "onCreate: webView>>" + webView);

    }

    public void loadTuxiClick(View view){
        handler.post(new Runnable() {
            @Override
            public void run() {
                loadPage();
            }
        });
    }

    public void loadOtherClick(View view){
        handler.post(new Runnable() {
            @Override
            public void run() {
                setupWebView(WebViewAty.this, webView, url,"Q5150-1");
            }
        });
    }

    public void setupWebView(Context context, WebView webView, String shopeeH5Url, String imei) {
        CookieSyncManager createInstance = CookieSyncManager.createInstance(context);
        CookieManager instance = CookieManager.getInstance();

        if (instance != null) {
            try {
                // 获取 host 名称
                String host = new URL(shopeeH5Url).getHost();

                // 构建第一个 Cookie
                StringBuilder cookie1 = new StringBuilder();
                cookie1.append("locker_device_id=").append(imei);
                cookie1.append(";domain=").append(host);
                cookie1.append(";path=/");

                // 构建第二个 Cookie
                StringBuilder cookie2 = new StringBuilder();
                cookie2.append("_SPC_PFB=pfb-dms-dev-spserp-9870-uat");
                cookie2.append(";domain=").append(host);
                cookie2.append(";path=/");

                // 设置第三方 Cookie 接受
                instance.setAcceptThirdPartyCookies(webView, true);
                instance.setAcceptCookie(true);

                // 清除旧的 Cookies
                instance.removeSessionCookie();
                instance.removeAllCookie();

                // 设置新的 Cookies
                instance.setCookie(shopeeH5Url, cookie1.toString());
                instance.setCookie(shopeeH5Url, cookie2.toString());

                // 同步 Cookies
                instance.flush();
                createInstance.sync();

                // 获取并打印 Cookies (用于调试)
                String cookie = instance.getCookie(host);
                Log.i("cookie", cookie);

                // 配置 WebView
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setUseWideViewPort(true);
                webSettings.setLoadWithOverviewMode(true);
                webSettings.setSupportZoom(false);
                webSettings.setDomStorageEnabled(true);
                webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webSettings.setMediaPlaybackRequiresUserGesture(false);
                webSettings.setDefaultTextEncodingName("utf-8");
                webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

                // 加载 URL
                webView.loadUrl(shopeeH5Url);

                // 设置 WebViewClient 以处理重定向
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        view.loadUrl(request.getUrl().toString());
                        return true;
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPage() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        // 设置Cookies
        String url = "https://h5.tuxi.com/#/privacy";
//        String url = "https://sp.spx.uat.shopee.sg/selfservice/locker";
//        String cookieValue = "name=value";
//        // 启用Cookie
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        cookieManager.setCookie(url, cookieValue);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "onPageStarted: ");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "onPageFinished: ");
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.d(TAG, "onReceivedError: ");
            }
        });
    }
}
