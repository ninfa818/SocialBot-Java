package com.hostcart.socialbot.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDex;

import com.google.android.gms.ads.MobileAds;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.main.MainActivity;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.AppVerUtil;
import com.hostcart.socialbot.utils.DetachableClickListener;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.PermissionsUtil;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

//this is the First Activity that launched when user starts the App
public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 451;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_splash_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_splash_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (getResources().getBoolean(R.bool.are_ads_enabled))
                MobileAds.initialize(SplashActivity.this, getString(R.string.admob_app_id));

            if (PermissionsUtil.hasPermissions(SplashActivity.this)) {
                if (!FireManager.isLoggedIn())
                    startLoginActivity();
                else
                    startNextActivity();
            } else {
                requestPermissions();
            }
        }, 1500);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions, PERMISSION_REQUEST_CODE);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, VerifyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startNextActivity() {
        FireManager.addFriendsToRealm(this);
        if (SharedPreferencesManager.isUserInfoSaved()) {
            boolean isFirst = SharedPreferencesManager.getFlagFirst();
            if (isFirst) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, InformationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } else {
            AppVerUtil.isLogin = true;
            Intent intent = new Intent(this, MyProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showAlertDialog() {
        DetachableClickListener positiveClickListener = DetachableClickListener.wrap((dialogInterface, i) -> requestPermissions());
        DetachableClickListener negativeClickListener = DetachableClickListener.wrap((dialogInterface, i) -> finish());

        AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions)
                .setMessage(R.string.you_have_to_grant_permissions)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
                .create();

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder);
        negativeClickListener.clearOnDetach(builder);
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!FireManager.isLoggedIn())
                startLoginActivity();
            else
                startNextActivity();
        } else
            showAlertDialog();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}



