package com.hostcart.socialbot.vmeet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.google.firebase.database.FirebaseDatabase;
import com.hostcart.socialbot.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class SharedObjects extends MultiDexApplication {

    public static Context context;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    public static int PRIVATE_MODE = 0;
    public static String PREF_NAME = "VMeet";

    private static SharedObjects instance;

    public static SharedObjects getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
        // or return instance.getApplicationContext();
    }

    public SharedObjects() {
    }

    public SharedObjects(Context context) {
        this.context = context;
        initializeStetho();

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        sharedPreference = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreference.edit();

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/montserrat_regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
    }

    @Override
    public void onCreate() {
        instance = new SharedObjects(this);
        super.onCreate();
        context = getApplicationContext();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public void initializeStetho() {
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(context);
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context));
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(context));
        Stetho.Initializer initializer = initializerBuilder.build();
        Stetho.initialize(initializer);
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static void hideKeyboard(View view, Context c) {
        InputMethodManager inputMethodManager = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    public static String getVersion(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getDeviceVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getTodaysDate(String dateFormat) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.getDefault());
        return df.format(c.getTime());
    }

    public static String convertDateFormat(String dateString, String originalDateFormat, String outputDateFormat) {
        String finalDate = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(originalDateFormat, Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(dateString);
            simpleDateFormat = new SimpleDateFormat(outputDateFormat, Locale.getDefault());
            finalDate = simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return finalDate;
    }


}
