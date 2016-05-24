package com.byteera.bank.activity;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.byteera.R;

import org.apache.commons.lang3.StringUtils;

public class WebViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_web_view);

        String content = getIntent().getStringExtra("content");

        if(!StringUtils.isEmpty(content))
        {
            WebView webView = (WebView)findViewById(R.id.webview);
            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        }
        else
        {
            this.finish();
        }
    }
}
