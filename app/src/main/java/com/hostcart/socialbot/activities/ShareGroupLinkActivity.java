package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.model.realms.Group;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.ClipboardUtil;
import com.hostcart.socialbot.utils.GroupLinkUtil;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.MessageCreator;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.TextViewDrawableCompat;

import java.util.List;

public class ShareGroupLinkActivity extends AppCompatActivity {

    private static final int REQUEST_SHARE_VIA_FIREAPP = 23;
    private LinearLayout shareLinkLayout;
    private TextView tvGroupLink;
    private TextViewDrawableCompat tvSendLinkViaFireapp;
    private TextViewDrawableCompat tvCopyLink;
    private TextViewDrawableCompat tvShareLink;
    private TextViewDrawableCompat tvRevokeLink;
    private ProgressBar progressBar;

    private Group group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_share_group_link_dark);
        } else {
            setContentView(R.layout.activity_share_group_link_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        shareLinkLayout = findViewById(R.id.share_link_layout);
        tvGroupLink = findViewById(R.id.tv_group_link);
        tvSendLinkViaFireapp = findViewById(R.id.tv_send_link_via_fireapp);
        tvCopyLink = findViewById(R.id.tv_copy_link);
        tvShareLink = findViewById(R.id.tv_share_link);
        tvRevokeLink = findViewById(R.id.tv_revoke_link);
        progressBar = findViewById(R.id.progress_bar);

        String sendLinkText = String.format(getString(R.string.send_link_via_fireapp), getString(R.string.app_name));
        tvSendLinkViaFireapp.setText(sendLinkText);

        final String groupId = getIntent().getStringExtra(IntentUtils.EXTRA_GROUP_ID);
        final User user = RealmHelper.getInstance().getUser(groupId);

        disableClicks();
        if (user != null && user.getGroup() != null) {
            group = user.getGroup();
            if (group.getCurrentGroupLink() != null) {
                enableClicks();
                setLinkText(group.getCurrentGroupLink());
            } else {
                tvGroupLink.setText(R.string.no_link_gnerated);
                GroupLinkUtil.getLinkAndFetchNewOneIfNotExists(groupId, new GroupLinkUtil.FetchCurrentLinkCallback() {
                    @Override
                    public void onFetch(String groupLink) {
                        enableClicks();
                        setLinkText(groupLink);
                    }

                    @Override
                    public void onFailed() {
                        disableClicks();
                    }
                });
            }
        }

        tvSendLinkViaFireapp.setOnClickListener(view -> {
            Intent intent = new Intent(ShareGroupLinkActivity.this, ForwardActivity.class);
            startActivityForResult(intent, REQUEST_SHARE_VIA_FIREAPP);
        });

        shareLinkLayout.setOnClickListener(view -> {
            if (group.getCurrentGroupLink() != null) {
                startActivity(IntentUtils.getShareTextIntent(getLink()));
            }
        });

        tvCopyLink.setOnClickListener(view -> {
            ClipboardUtil.copyTextToClipboard(ShareGroupLinkActivity.this, getLink());
            Toast.makeText(ShareGroupLinkActivity.this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        });

        tvShareLink.setOnClickListener(view -> startActivity(IntentUtils.getShareTextIntent(getLink())));

        tvRevokeLink.setOnClickListener(view -> {
            hideOrShowProgress(true);
            GroupLinkUtil.generateLink(groupId, new GroupLinkUtil.GenerateLinkCallback() {
                @Override
                public void onGenerate(String groupLink) {
                    setLinkText(groupLink);
                    hideOrShowProgress(false);
                }

                @Override
                public void onFailed() { }
            });
        });
    }

    private void setLinkText(String groupLink) {
        tvGroupLink.setText(GroupLinkUtil.getFinalLink(groupLink));
    }


    @NonNull
    private String getLink() {
        return tvGroupLink.getText().toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHARE_VIA_FIREAPP && resultCode == RESULT_OK) {
            List<User> selectedUsers = data.getParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT);
            String link = getLink();
            for (User selectedUser : selectedUsers) {
                Message message = new MessageCreator.Builder(selectedUser, MessageType.SENT_TEXT).text(link).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            }

            Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
        }
    }

    private void disableClicks() {
        tvShareLink.setEnabled(false);
        tvSendLinkViaFireapp.setEnabled(false);
        tvRevokeLink.setEnabled(false);
        tvCopyLink.setEnabled(false);
        shareLinkLayout.setEnabled(false);
        hideOrShowProgress(true);
    }

    private void hideOrShowProgress(boolean showProgress) {
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        shareLinkLayout.setVisibility(showProgress ? View.GONE : View.VISIBLE);
    }

    private void enableClicks() {
        tvShareLink.setEnabled(true);
        tvSendLinkViaFireapp.setEnabled(true);
        tvRevokeLink.setEnabled(true);
        tvCopyLink.setEnabled(true);
        shareLinkLayout.setEnabled(true);
        hideOrShowProgress(false);
    }

}
