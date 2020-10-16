package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.model.constants.FireCallType;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private CircleImageView imageViewUserProfile;
    private TextView tvUsername;
    private TextView tvSurname;
    private TextView tvEmail;
    private TextView tvGender;
    private TextView tvStatus;
    private TextView tvBirth;
    private TextView tvPhoneNumber;
    private Button btn_chat, btn_voice, btn_video, btn_add;

    private UserInfo user;


    @SuppressLint("SetTextI18n")
    private void initWithEvent() {
        btn_add.setOnClickListener(v -> {
            if (user != null) {
                if (btn_add.getText().toString().equals("Add") ) {
                    btn_add.setEnabled(false);
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(user.getUid()).setValue(UUID.randomUUID().toString())
                            .addOnCompleteListener(task -> FireConstants.friendRequestRef.child(user.getUid()).child("received")
                                    .child(FireManager.getUid()).setValue(UUID.randomUUID().toString()).addOnCompleteListener(task1 -> {
                                        btn_add.setText("Added");
                                        btn_add.setEnabled(true);
                                    }));
                } else {
                    btn_add.setEnabled(false);
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(user.getUid()).removeValue()
                            .addOnCompleteListener(task -> FireConstants.friendRequestRef.child(user.getUid()).child("received")
                                    .child(FireManager.getUid()).removeValue().addOnCompleteListener(task12 -> {
                                        btn_add.setText("Add");
                                        btn_add.setEnabled(true);
                                    }));
                }
            }
        });
        btn_voice.setOnClickListener(v -> {
            if (user != null) {
                Intent callScreen = new Intent(this, CallingActivity.class);
                callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                callScreen.putExtra(IntentUtils.ISVIDEO, false);
                callScreen.putExtra(IntentUtils.UID, user.getUid());
                startActivity(callScreen);
            }
        });
        btn_video.setOnClickListener(v -> {
            if (user != null) {
                Intent callScreen = new Intent(this, CallingActivity.class);
                callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                callScreen.putExtra(IntentUtils.ISVIDEO, true);
                callScreen.putExtra(IntentUtils.UID, user.getUid());
                startActivity(callScreen);
            }
        });
        btn_chat.setOnClickListener(v -> {
            if (user != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.UID, user.getUid());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_user_profile_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_user_profile_light);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initWithView();
        initWithEvent();
    }

    private void initWithView() {
        imageViewUserProfile = findViewById(R.id.image_view_user_profile);
        tvUsername = findViewById(R.id.tv_username);
        tvSurname = findViewById(R.id.tv_surname);

        tvStatus = findViewById(R.id.tv_status);
        tvEmail = findViewById(R.id.tv_email);
        tvGender = findViewById(R.id.tv_gender);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvBirth = findViewById(R.id.tv_birth);

        btn_add = findViewById(R.id.btn_add);
        btn_chat = findViewById(R.id.btn_chat);
        btn_video = findViewById(R.id.btn_video);
        btn_voice = findViewById(R.id.btn_voice);

        initWithData();
    }

    private void initWithData() {
        ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.normal_server_error));
        FireManager.getUserInfoByUid(AppUtils.gUid, new FireManager.userInfoListener() {
            @Override
            public void onFound(UserInfo userInfo) {
                dialog.dismiss();
                user = userInfo;

                Glide.with(UserProfileActivity.this)
                        .asBitmap()
                        .load(userInfo.getPhoto())
                        .error(R.drawable.img1)
                        .into(imageViewUserProfile);

                tvUsername.setText(userInfo.getName());
                tvSurname.setText(userInfo.getSurname());
                tvEmail.setText(userInfo.getEmail());
                tvGender.setText(userInfo.getGender());
                tvStatus.setText(userInfo.getStatus());
                tvBirth.setText(userInfo.getBirthDate());
                tvPhoneNumber.setText(userInfo.getPhone());

                FireConstants.friendsRef.child(FireManager.getUid()).child(AppUtils.gUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        dialog.dismiss();
                        String phone = snapshot.getValue(String.class);
                        if (phone != null && phone.length() > 0) {
                            btn_chat.setVisibility(View.VISIBLE);
                            btn_voice.setVisibility(View.VISIBLE);
                            btn_video.setVisibility(View.VISIBLE);
                        } else {
                            FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent").child(user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            btn_add.setVisibility(View.VISIBLE);
                                            if( dataSnapshot.getValue() != null ) {
                                                btn_add.setText("Added");
                                            } else {
                                                btn_add.setText("Add");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onNotFound() {
                dialog.dismiss();
                Toast.makeText(UserProfileActivity.this, R.string.normal_server_error, Toast.LENGTH_SHORT).show();
            }
        });

        btn_add.setVisibility(View.GONE);
        btn_chat.setVisibility(View.GONE);
        btn_voice.setVisibility(View.GONE);
        btn_video.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
