package com.hostcart.socialbot.activities;

import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.RealmBackupRestore;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.TimeHelper;
import com.hostcart.socialbot.utils.Util;

import io.realm.internal.IOException;

public class BackupChatActivity extends AppCompatActivity {

    private TextView tvLastBackup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_backup_chat_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
        } else {
            setContentView(R.layout.activity_backup_chat_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        tvLastBackup = findViewById(R.id.tv_last_backup);
        Button btnBackup = findViewById(R.id.btn_backup);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!= null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setLastBackupTime();

        btnBackup.setOnClickListener(view -> {
            ProgressDialog progressDialog = new ProgressDialog(BackupChatActivity.this);
            progressDialog.setTitle(R.string.backing_up);
            progressDialog.setMessage(getResources().getString(R.string.backing_up_message));
            progressDialog.show();

            try {
                new RealmBackupRestore(BackupChatActivity.this).backup();
                Util.showSnackbar(BackupChatActivity.this, getResources().getString(R.string.backup_success));
            } catch (IOException e) {
                e.printStackTrace();
                Util.showSnackbar(BackupChatActivity.this, getResources().getString(R.string.backup_failed));
            }
            progressDialog.dismiss();
            setLastBackupTime();
        });
    }

    private void setLastBackupTime() {
        long lastBackupTime = SharedPreferencesManager.getLastBackup();
        if (lastBackupTime != -1) {
            tvLastBackup.setVisibility(View.VISIBLE);
            String backupTimeStr = TimeHelper.getLastBackupTime(lastBackupTime);
            tvLastBackup.setText(backupTimeStr);

        } else
            tvLastBackup.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}
