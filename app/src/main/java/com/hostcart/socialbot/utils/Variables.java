package com.hostcart.socialbot.utils;

import android.content.SharedPreferences;

/**
 * Created by AQEEL on 1/2/2019.
 */

public class Variables {
    
    public static String user_name="username";
    public static String user_id="id";
    public static SharedPreferences sharedPreferences;

    public static String pref="pref";
    public static String lat="latitude";
    public static String lng="longitude";
    public static String location="location";
    public static String temperature="temperature";
    public static String weather_icon="weather_icon";
    public static String api_request_time="api_request_time";
    public static String q_and_a="q_and_a";

    public static String main_domain = "http://socialbot.co.za/API/index.php?p=";
    public static String bootChat = main_domain + "bootChat";

    public static String wheather_api_key="b94770f5722b6aefb7f634515ae51a37";
    public static String weatherapi = "https://api.darksky.net/forecast/" + wheather_api_key + "/";

}
