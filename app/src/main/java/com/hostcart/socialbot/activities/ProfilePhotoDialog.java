package com.hostcart.socialbot.activities;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.Util;


public class ProfilePhotoDialog extends AppCompatActivity {

    private ImageView imageViewUserProfileDialog;
    private TextView tvUsernameDialog;

    private ImageButton buttonInfoDialog;
    private ImageButton buttonMessageDialog;
    private User user;

    private boolean isBroadcast;

    FireManager.OnGetUserPhoto onGetUserPhoto;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!Util.isOreoOrAbove()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile_photo_dialog);

        initViews();

        String uid = getIntent().getStringExtra(IntentUtils.UID);
        user = RealmHelper.getInstance().getUser(uid);
        isBroadcast = user.isBroadcastBool();
        tvUsernameDialog.setText(user.getUserName());

        loadUserImg();

        //show the image in ProfilePhotoActivity
        imageViewUserProfileDialog.setOnClickListener(v -> {
            Intent intent = new Intent(ProfilePhotoDialog.this, ProfilePhotoActivity.class);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        //show the user info
        buttonInfoDialog.setOnClickListener(view -> {
            Intent intent = new Intent(ProfilePhotoDialog.this, UserDetailsActivity.class);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        //start Chat with this user
        buttonMessageDialog.setOnClickListener(view -> {
            Intent intent = new Intent(ProfilePhotoDialog.this, ChatActivity.class);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserImg() {
        if (isBroadcast) {
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.ic_broadcast_with_bg);
            imageViewUserProfileDialog.setImageDrawable(drawable);
        } else if (user.getUserLocalPhoto() != null && FileUtils.isFileExists(user.getUserLocalPhoto())) {
            Glide.with(this)
                    .load(user.getUserLocalPhoto())
                    .into(imageViewUserProfileDialog);

            //otherwise show thumbImg if it's exists
        } else if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(this).asBitmap().load(bytes).into(imageViewUserProfileDialog);
        }
    }

    private void initViews() {
        imageViewUserProfileDialog = findViewById(R.id.image_view_user_profile_dialog);
        tvUsernameDialog = findViewById(R.id.tv_username_dialog);
        buttonInfoDialog = findViewById(R.id.button_info_dialog);
        buttonMessageDialog = findViewById(R.id.button_message_dialog);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onGetUserPhoto = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //load user info once it's downloaded
        onGetUserPhoto = new FireManager.OnGetUserPhoto() {
            @Override
            //load thumb img while the full image is downloading
            public void onGetThumb(String thumbImg) {
                try {
                    Glide.with(ProfilePhotoDialog.this)
                            .asBitmap()
                            .load(BitmapUtils.encodeImageAsBytes(thumbImg))
                            .into(imageViewUserProfileDialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onGetPhoto(String photoPath) {
                try {
                    Glide.with(ProfilePhotoDialog.this).load(photoPath).into(imageViewUserProfileDialog);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        //check if there is a new image for this user
        //if yes ,download it and show it
        if (!isBroadcast)
        FireManager.checkAndDownloadUserPhoto(user, onGetUserPhoto);
    }

}
