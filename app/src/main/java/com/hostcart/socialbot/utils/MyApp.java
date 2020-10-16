package com.hostcart.socialbot.utils;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.SplashActivity;
import com.hostcart.socialbot.job.FireJobCreator;
import com.evernote.android.job.JobManager;
import com.google.android.gms.ads.MobileAds;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MyApp extends Application {
    private static MyApp mApp = null;
    private static String currentChatId = "";
    private static boolean phoneCallActivityVisible;
    private static boolean baseActivityVisible;
    private static boolean isCallActive = false;
    private static boolean isPersistenceEnabled = false;

    public static String getCurrentChatId() {
        return currentChatId;
    }

    public static void chatActivityResumed(String chatId) {
        currentChatId = chatId;
    }

    public static void chatActivityPaused() {
        currentChatId = "";
    }

    public static boolean isPhoneCallActivityVisible() {
        return phoneCallActivityVisible;
    }

    public static void phoneCallActivityResumed() {
        phoneCallActivityVisible = true;
    }

    public static void phoneCallActivityPaused() {
        phoneCallActivityVisible = false;
    }

    public static boolean isBaseActivityVisible() {
        return baseActivityVisible;
    }

    public static void baseActivityResumed() {
        baseActivityVisible = true;
    }

    public static void baseActivityPaused() {
        baseActivityVisible = false;
    }

    public static void setCallActive(boolean mCallActive) {
        isCallActive = mCallActive;
    }

    public static boolean isIsCallActive() {
        return isCallActive;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (!isPersistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistenceEnabled = true;
        }
        SharedPreferencesManager.init(this);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(MyMigration.SCHEMA_VERSION)
                .migration(new MyMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        JobManager.create(this).addJobCreator(new FireJobCreator());
        mApp = this;
    }

    public static Context context() {
        return mApp.getApplicationContext();
    }

}
