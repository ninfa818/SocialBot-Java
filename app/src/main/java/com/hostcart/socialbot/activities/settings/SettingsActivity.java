package com.hostcart.socialbot.activities.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.BackupChatActivity;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.dialogs.IgnoreBatteryDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else if (preference instanceof RingtonePreference) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.pref_ringtone_silent);

            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.PreferenceScreenLight);
        } else {
            setTheme(R.style.PreferenceScreen);
        }
        super.onCreate(savedInstanceState);
        setupActionBar();
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getListView().setBackgroundColor(getColor(R.color.colorNewDark));

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            loadHeadersFromResource(R.xml.pref_headers_dark, target);
        } else {
            loadHeadersFromResource(R.xml.pref_headers_light, target);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ProfilePreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || ChatSettingsPreferenceFragment.class.getName().equals(fragmentName)
                || AboutFragment.class.getName().equals(fragmentName)
                || PrivacyPolicyFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void startPreferenceFragment(Fragment fragment, boolean push) {
        super.startPreferenceFragment(fragment, push);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
            findPreference("ignore_battery").setOnPreferenceClickListener(preference -> {
                IgnoreBatteryDialog ignoreBatteryDialog = new IgnoreBatteryDialog(getActivity());
                ignoreBatteryDialog.setOnDialogClickListener(new IgnoreBatteryDialog.OnDialogClickListener() {
                    @Override
                    public void onCancelClick(boolean checkBoxChecked) {
                        SharedPreferencesManager.setDoNotShowBatteryOptimizationAgain(checkBoxChecked);
                    }

                    @Override
                    public void onOk() {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    }
                });
                ignoreBatteryDialog.show();
                return false;
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                view.setBackgroundColor(getResources().getColor(R.color.colorNew));
            }
            return view;
        }
    }

    public static class ChatSettingsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_chat);
            findPreference("wallpaper_path").setOnPreferenceClickListener(preference -> {
                if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogDark);
                    dialog.setNegativeButton(R.string.choose_wallpaper, (dialogInterface, i) -> CropImage.activity().start(getActivity(), ChatSettingsPreferenceFragment.this))
                            .setNeutralButton(R.string.restore_default_wallpaper, (dialogInterface, i) -> SharedPreferencesManager.setWallpaperPath("")).show();
                } else {
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogLight);
                    dialog.setNegativeButton(R.string.choose_wallpaper, (dialogInterface, i) -> CropImage.activity().start(getActivity(), ChatSettingsPreferenceFragment.this))
                            .setNeutralButton(R.string.restore_default_wallpaper, (dialogInterface, i) -> SharedPreferencesManager.setWallpaperPath("")).show();
                }
                return false;
            });

            findPreference("chat_backup").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getActivity(), BackupChatActivity.class));
                return false;
            });
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    File file = DirManager.genereateWallpaperFile();
                    try {
                        FileUtils.copyFile(resultUri.getPath(), file);
                        SharedPreferencesManager.setWallpaperPath(file.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                view.setBackgroundColor(getResources().getColor(R.color.colorNew));
            }
            return view;
        }
    }

}
