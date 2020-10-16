package com.hostcart.socialbot.utils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URLEncoder;

public class HttpTranslate {

    private static final String BASE_URL = "https://translation.googleapis.com/language/translate/v2?";
    private static final String KEY = "YOUR_KEY_HERE";


    private static AsyncHttpClient client = new AsyncHttpClient();


    public static void post(String transText,String sourceLang, String destLang, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(transText, sourceLang, destLang), responseHandler);
    }

    private static String makeKeyChunk(String key) {
        return "key=" + KEY;
    }

    private static String makeTransChunk(String transText) {
        String encodedText = URLEncoder.encode(transText);
        return "&amp;q=" + encodedText;
    }

    private static String langSource(String langSource) {
        return "&amp;source=" + langSource;
    }

    private static String langDest(String langDest) {
        return "&amp;target=" + langDest;

    }

    private static String getAbsoluteUrl(String transText, String sourceLang, String destLang) {
        String apiUrl = BASE_URL + makeKeyChunk(KEY) + makeTransChunk(transText) + langSource(sourceLang) + langDest(destLang);
        return apiUrl;
    }

}
