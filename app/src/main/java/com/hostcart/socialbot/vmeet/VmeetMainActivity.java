package com.hostcart.socialbot.vmeet;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.main.MainActivity;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.vmeet.firebase_db.DatabaseManager;
import com.hostcart.socialbot.vmeet.home.HomeFragment;
import com.hostcart.socialbot.vmeet.meeting_history.MeetingHistoryFragment;
import com.hostcart.socialbot.vmeet.schedule.ScheduleFragment;
import com.hostcart.socialbot.vmeet.utils.SharedObjects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class VmeetMainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    boolean doubleBackToExitPressedOnce = false;
    DatabaseManager databaseManager;
    SharedObjects sharedObjects;

    BottomNavigationView navigation;
    Toolbar toolbar;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_vmeet_main_dark);

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
//            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_vmeet_main_light);

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        sharedObjects = new SharedObjects(VmeetMainActivity.this);

        //get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();
        databaseManager = new DatabaseManager();
        databaseManager.setOnUserListener(new DatabaseManager.OnUserListener() {
            @Override
            public void onUserFound() {
                updateFragments();
            }

            @Override
            public void onUserNotFound() {
                removeAllPreferenceOnLogout();
            }
        });

        FirebaseAuth.AuthStateListener authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) { databaseManager.getUser(user.getUid()); }
        };
        firebaseAuth.addAuthStateListener(authListener);

        loadFragment(new HomeFragment());
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SharedObjects.isNetworkConnected(VmeetMainActivity.this)) {
            AppUpdaterUtils appUpdaterUtils = new AppUpdaterUtils(this)
                    .withListener(new AppUpdaterUtils.UpdateListener() {
                        @Override
                        public void onSuccess(Update update, Boolean isUpdateAvailable) {
                            if (isUpdateAvailable) {
                                launchUpdateDialog(update.getLatestVersion());
                            }
                        }

                        @Override
                        public void onFailed(AppUpdaterError error) {

                        }
                    });
            appUpdaterUtils.start();
        }
    }

    private void launchUpdateDialog(String onlineVersion) {
        try {
            MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(VmeetMainActivity.this);
            materialAlertDialogBuilder.setMessage("Update " + onlineVersion + " is available to download. Downloading the latest update you will get the latest features," +
                    "improvements and bug fixes of " + getString(R.string.app_name));
            materialAlertDialogBuilder.setCancelable(false).setPositiveButton(getResources().getString(R.string.update_now), (dialog, which) -> {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            });
            materialAlertDialogBuilder.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFragments() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).setUserData();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = menuItem -> {
        Fragment fragment;
                Class fragmentClass;
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        fragmentClass = HomeFragment.class;
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                            loadFragment(fragment, menuItem);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                    case R.id.nav_meeting_history:
                        fragmentClass = MeetingHistoryFragment.class;
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                            loadFragment(fragment, menuItem);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;

                    case R.id.nav_schedule:
                        fragmentClass = ScheduleFragment.class;
                        try {
                            fragment = (Fragment) fragmentClass.newInstance();
                            loadFragment(fragment, menuItem);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return true;
                }
                return false;
            };

    @Override
    protected void onResume() {
        super.onResume();
        this.doubleBackToExitPressedOnce = false;
    }

    public void loadFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.flContent, fragment, backStateName);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
    }

    public void loadFragment(Fragment fragment, MenuItem menuItem) {
        String backStateName = fragment.getClass().getName();

        FragmentManager manager = getSupportFragmentManager();
        boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.flContent, fragment, backStateName);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(backStateName);
            ft.commit();
            menuItem.setChecked(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void removeAllPreferenceOnLogout() {
        try {
            firebaseAuth.signOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        AppUtils.showOtherActivity(this, MainActivity.class, 1);
        finish();
    }

}
