package com.hostcart.socialbot.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.ContactDetailsAdapter;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.PhoneNumber;
import com.hostcart.socialbot.model.realms.RealmContact;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.ClipboardUtil;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.NetworkHelper;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.SnackbarUtil;

public class ContactDetailsActivity extends AppCompatActivity {

    private DialogInterface b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_contact_details_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_contact_details_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        TextView tvContactNameDetails = findViewById(R.id.tv_contact_name_details);
        RecyclerView recyclerView = findViewById(R.id.rv_contact_details);
        Button btnAddContact = findViewById(R.id.btn_add_contact);

        if (!getIntent().hasExtra(IntentUtils.EXTRA_MESSAGE_ID))
            return;

        String id = getIntent().getStringExtra(IntentUtils.EXTRA_MESSAGE_ID);
        String chatId = getIntent().getStringExtra(IntentUtils.EXTRA_CHAT_ID);
        Message message = RealmHelper.getInstance().getMessage(id, chatId);
        if (message == null)
            return;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.contact_info);
        }
        final RealmContact contact = message.getContact();
        tvContactNameDetails.setText(contact.getName());
        ContactDetailsAdapter adapter = new ContactDetailsAdapter(contact.getRealmList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddContact.setOnClickListener(v -> startActivity(IntentUtils.getAddContactIntent(contact)));

        adapter.setOnItemClick(new ContactDetailsAdapter.OnItemClick() {
            @Override
            public void onItemClick(View view, int pos) {
                if (!NetworkHelper.isConnected(ContactDetailsActivity.this)) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.no_internet_connection, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                PhoneNumber phoneNumber = contact.getRealmList().get(pos);
                showProgress();
                FireManager.isHasFireApp(phoneNumber.getNumber(), new FireManager.IsHasAppListener() {
                    @Override
                    public void onFound(User user) {
                        hideProgress();
                        startChatActivityWithDifferentUser(user);
                    }

                    @Override
                    public void onNotFound() {
                        hideProgress();
                        SnackbarUtil.showDoesNotFireAppSnackbar(ContactDetailsActivity.this);
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, int pos) {
                PhoneNumber phoneNumber = contact.getRealmList().get(pos);
                ClipboardUtil.copyTextToClipboard(ContactDetailsActivity.this, phoneNumber.getNumber());
                Toast.makeText(ContactDetailsActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void showProgress() {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogDark);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);
            dialogBuilder.show();

            b = dialogBuilder.create();
        } else {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogLight);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
            dialogBuilder.setView(dialogView);
            dialogBuilder.setCancelable(true);
            dialogBuilder.show();

            b = dialogBuilder.create();
        }
    }

    public void hideProgress() {
        b.dismiss();
    }

    private void startChatActivityWithDifferentUser(User user) {
        Intent intent = new Intent(ContactDetailsActivity.this, ChatActivity.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}
