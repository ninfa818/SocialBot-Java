package com.hostcart.socialbot.utils;

import android.app.Activity;
import android.content.Intent;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientImpl extends WebViewClient {
    private Activity activity = null;

    public WebViewClientImpl(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        String url = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            url = request.getUrl().getPath();
        } else
            return false;

        if( url.indexOf("journaldev.com") > -1 ) return false;

        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
        } else
            return false;

        activity.startActivity(intent);
        return true;
    }
}
