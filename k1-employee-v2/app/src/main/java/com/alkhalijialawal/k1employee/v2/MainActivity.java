package com.alkhalijialawal.k1employee.v2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(this), "Android");

        // Compile-time packaged resource. If this file is missing, the APK build fails.
        String html = readRawHtml();
        webView.loadDataWithBaseURL(
            "https://k1employee.local/",
            html,
            "text/html",
            "UTF-8",
            null
        );
    }

    private String readRawHtml() {
        StringBuilder out = new StringBuilder();
        try {
            InputStream is = getResources().openRawResource(R.raw.employee);
            BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
            );
            String line;
            while ((line = br.readLine()) != null) {
                out.append(line).append("\n");
            }
            br.close();
        } catch (Exception e) {
            return "<!doctype html><html lang='ar' dir='rtl'><meta charset='utf-8'>" +
                   "<body style='font-family:sans-serif;padding:24px'>" +
                   "<h2>تعذر تشغيل التطبيق</h2><p>خطأ داخلي في قراءة واجهة K1 V2.</p></body></html>";
        }
        return out.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public static class AndroidBridge {
        private final Context context;

        AndroidBridge(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void copyText(String text) {
            ClipboardManager cm =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(
                ClipData.newPlainText("K1 Order", text == null ? "" : text)
            );
        }
    }
}
