package com.hostcart.socialbot.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

import com.hostcart.socialbot.utils.MyApp;
import com.hostcart.socialbot.utils.PresenceUtil;

abstract public class BaseActivity extends AppCompatActivity {

    public abstract boolean enablePresence();

    private PresenceUtil presenceUtil;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (enablePresence())
            presenceUtil = new PresenceUtil();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (enablePresence()) {
            presenceUtil.onResume();
            MyApp.baseActivityResumed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (enablePresence()) {
            presenceUtil.onPause();
            MyApp.baseActivityPaused();
        }
    }


}
