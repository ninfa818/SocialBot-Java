package com.hostcart.socialbot.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.main.MainActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.AppVerUtil;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.CropImageRequest;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.NetworkHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyProfileActivity extends AppCompatActivity {

    public static final int REQUEST_LANG = 4000;

    private CircleImageView imageViewUserProfile;

    private ImageButton changeUserProfile;
    private ImageButton editUsername;
    private ImageButton editSurname;
    private ImageButton editEmail;
    private ImageButton editGender;
    private ImageButton editStatus;
    private ImageButton editLanguage;
    private ImageButton editBirth;

    private TextView tvUsername;
    private TextView tvSurname;
    private TextView tvEmail;
    private TextView tvGender;
    private TextView tvStatus;
    private TextView tvLanguage;
    private TextView tvBirth;

    private List<UserInfo> remainList = new ArrayList<>();

    private Button addedButton, friendsButton, addButton;
    private ImageView img_check_light, img_check_dark, img_light, img_dark;

    private int invitedCount = 0;
    private int friendCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_my_profile_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_my_profile_light);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initUIView();

        // init click listener
        final String myPhoto = SharedPreferencesManager.getMyPhoto();
        initClickGroup(myPhoto);

        friendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, FriendsActivity.class);
            startActivity(intent);
        });

        addedButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, AddedMeActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, QuickAddActivity.class);
            startActivity(intent);
        });

        img_light.setOnClickListener(v -> {
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogDark);
                builder.setTitle(R.string.alert_theme_title)
                        .setMessage(R.string.alert_theme_desk)
                        .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                            SharedPreferencesManager.setThemeMode(AppUtils.THEME_LIGHT);
                            AppUtils.showOtherActivity(this, MainActivity.class, 1);
                            finish();
                        })
                        .setNeutralButton(R.string.cancel, (dialogInterface, i) -> {});
                builder.show();
            }
        });

        img_dark.setOnClickListener(v -> {
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogLight);
                builder.setTitle(R.string.alert_theme_title)
                        .setMessage(R.string.alert_theme_desk)
                        .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                            SharedPreferencesManager.setThemeMode(AppUtils.THEME_DARK);
                            AppUtils.showOtherActivity(this, MainActivity.class, 1);
                            finish();
                        })
                        .setNeutralButton(R.string.cancel, (dialogInterface, i) -> {});
                builder.show();
            }
        });

        if (AppVerUtil.isLogin) {
            FireConstants.usersRef.child(FireManager.getUid()).child("phone").setValue(FireManager.getPhoneNumber());
        }
    }

    private void initUIView() {
        imageViewUserProfile = findViewById(R.id.image_view_user_profile);
        changeUserProfile = findViewById(R.id.image_button_change_user_profile);
        tvUsername = findViewById(R.id.tv_username);
        tvSurname = findViewById(R.id.tv_surname);
        editUsername = findViewById(R.id.image_button_edit_username);
        editSurname = findViewById(R.id.image_button_edit_surname);
        editEmail = findViewById(R.id.edit_email);
        editGender = findViewById(R.id.edit_gender);
        editStatus = findViewById(R.id.edit_status);
        editLanguage = findViewById(R.id.edit_language);
        editBirth = findViewById(R.id.edit_birth);

        tvStatus = findViewById(R.id.tv_status);
        tvEmail = findViewById(R.id.tv_email);
        tvGender = findViewById(R.id.tv_gender);
        tvLanguage = findViewById(R.id.tv_Language);
        tvBirth = findViewById(R.id.tv_birth);

        img_light = findViewById(R.id.img_light);
        img_dark = findViewById(R.id.img_dark);
        img_check_light = findViewById(R.id.img_check_light);
        img_check_dark = findViewById(R.id.img_check_dark);

        friendsButton = findViewById(R.id.friends_button);
        addedButton = findViewById(R.id.added_button);
        addButton = findViewById(R.id.add_button);

        TextView tvPhoneNumber = findViewById(R.id.tv_phone_number);
        String phoneNumber = SharedPreferencesManager.getPhoneNumber();
        tvPhoneNumber.setText(phoneNumber);
    }

    private void initWithData() {
        if (AppVerUtil.isLogin) {
            tvUsername.setText(SharedPreferencesManager.getUserName());
            tvStatus.setText(SharedPreferencesManager.getStatus());
            tvSurname.setText(SharedPreferencesManager.getSurname());
            tvEmail.setText(SharedPreferencesManager.getEmail());
            tvGender.setText(SharedPreferencesManager.getGender());
            tvBirth.setText(SharedPreferencesManager.getBirthday());

            String photoUri = SharedPreferencesManager.getMyPhoto();
            Glide.with(this)
                    .asBitmap()
                    .load(photoUri)
                    .into(imageViewUserProfile);
        } else {
            FireManager.getUserInfoByUid(FireManager.getUid(), new FireManager.userInfoListener() {
                @Override
                public void onFound(UserInfo userInfo) {
                    tvUsername.setText(userInfo.getName());
                    SharedPreferencesManager.saveMyUsername(userInfo.getName());
                    tvStatus.setText(userInfo.getStatus());
                    SharedPreferencesManager.saveMyStatus(userInfo.getStatus());
                    tvSurname.setText(userInfo.getSurname());
                    SharedPreferencesManager.saveSurname(userInfo.getSurname());
                    tvEmail.setText(userInfo.getEmail());
                    SharedPreferencesManager.saveEmail(userInfo.getEmail());
                    tvGender.setText(userInfo.getGender());
                    SharedPreferencesManager.saveGender(userInfo.getGender());
                    tvBirth.setText(userInfo.getBirthDate());
                    SharedPreferencesManager.saveBirthday(userInfo.getBirthDate());

                    String photoUri = userInfo.getPhoto();
                    SharedPreferencesManager.saveMyPhoto(photoUri);
                    Glide.with(MyProfileActivity.this)
                            .asBitmap()
                            .load(photoUri)
                            .into(imageViewUserProfile);
                }

                @Override
                public void onNotFound() {

                }
            });
        }
        tvLanguage.setText(SharedPreferencesManager.getLanguage());
        setThemeView();

        loadInvitedList();
        loadRemainList();
        loadFriendListJsonString();
    }

    private void setThemeView() {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            img_check_light.setVisibility(View.GONE);
            img_check_dark.setVisibility(View.VISIBLE);
        } else {
            img_check_light.setVisibility(View.VISIBLE);
            img_check_dark.setVisibility(View.GONE);
        }
    }

    private void loadInvitedList() {
        FireConstants.friendRequestRef.child(Objects.requireNonNull(FireManager.getUid()))
                .child("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                invitedCount = 0;
                for (DataSnapshot ignored : snapshot.getChildren()) {
                    invitedCount++;
                }
                if (invitedCount > 0) {
                    addedButton.setEnabled(true);
                    addedButton.setText(String.format(Locale.US, "Added Me (%d)", invitedCount));
                } else {
                    addedButton.setEnabled(false);
                    addedButton.setText("Added Me");
                }

                setRemainList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadRemainList() {
        FireConstants.usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                remainList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    remainList.add(userInfo);
                }
                setRemainList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadFriendListJsonString() {
        FireConstants.friendsRef.child(Objects.requireNonNull(FireManager.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                friendCount = 0;
                for (DataSnapshot ignored : snapshot.getChildren()) {
                    friendCount++;
                }
                if (friendCount == 0) {
                    friendsButton.setEnabled(false);
                    friendsButton.setText("Friends");
                } else {
                    friendsButton.setEnabled(true);
                    friendsButton.setText(String.format(Locale.US, "Friends (%d)", friendCount));
                }

                setRemainList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setRemainList() {
        if (remainList.size() == 0) {
            addButton.setEnabled(false);
            return;
        }
        int userCount = remainList.size() - invitedCount - friendCount - 1;
        if(userCount > 0) {
            addButton.setEnabled(true);
            addButton.setText(String.format(Locale.US, "Quick Add (%d)", userCount));
        } else {
            addButton.setEnabled(false);
            addButton.setText("Quick Add");
        }
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(this);
    }

    private void initClickGroup(final String photo) {
        imageViewUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, ProfilePhotoActivity.class);
            String transName = "profile_photo_trans";

            intent.putExtra(IntentUtils.EXTRA_PROFILE_PATH, photo);
            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(MyProfileActivity.this, v, transName).toBundle());
        });

        // change picture image
        changeUserProfile.setOnClickListener(v -> pickImages());

        // full name
        editUsername.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_name), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(MyProfileActivity.this, R.string.username_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                FireManager.updateMyUserName(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveMyUsername(text);
                        saveValueToFirebase("name", text);
                        tvUsername.setText(text);
                    } else {
                        Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // surname
        editSurname.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_surname), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(MyProfileActivity.this, R.string.surname_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                FireManager.updateMySurname(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveSurname(text);
                        tvSurname.setText(text);
                    } else {
                        Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // status
        editStatus.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_status), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(MyProfileActivity.this, R.string.status_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                FireManager.updateMyStatus(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveMyStatus(text);
                        saveValueToFirebase("status", text);
                        tvStatus.setText(text);
                    } else {
                        Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // email
        editEmail.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_email), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(MyProfileActivity.this, R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }
            if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                FireManager.updateMyEmail(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveEmail(text);
                        tvEmail.setText(text);
                    } else {
                        Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // Gender
        editGender.setOnClickListener(v -> {
            String[] listItems = {"Male", "Female"};

            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MyProfileActivity.this, R.style.AlertDialogDark);
                builder.setTitle("Choose gender");
                int checkedItem = 0; //this will checked the item when user open the dialog
                builder.setSingleChoiceItems(listItems, checkedItem, (dialog, which) -> tvGender.setText(listItems[which]));
                builder.setNegativeButton("Done", (dialog, which) -> {
                    String text = tvGender.getText().toString();
                    if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                        FireManager.updateMyGender(text, isSuccessful -> SharedPreferencesManager.saveGender(text));
                    }
                    dialog.dismiss();
                });
                builder.show();
            } else {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MyProfileActivity.this, R.style.AlertDialogLight);
                builder.setTitle("Choose gender");
                int checkedItem = 0; //this will checked the item when user open the dialog
                builder.setSingleChoiceItems(listItems, checkedItem, (dialog, which) -> tvGender.setText(listItems[which]));
                builder.setNegativeButton("Done", (dialog, which) -> {
                    String text = tvGender.getText().toString();
                    if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                        FireManager.updateMyGender(text, isSuccessful -> SharedPreferencesManager.saveGender(text));
                    }
                    dialog.dismiss();
                });
                builder.show();
            }
        });

        // set birthday
        editBirth.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(
                    MyProfileActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth, (view, year, month, dayOfMonth) -> {
                String birthText =dayOfMonth + "/" + (month + 1) + "/" + year;
                if (NetworkHelper.isConnected(MyProfileActivity.this)) {
                    FireManager.updateMyBirthday(birthText, isSuccessful -> {
                        if (isSuccessful) {
                            SharedPreferencesManager.saveBirthday(birthText);
                            tvBirth.setText(birthText);
                        } else {
                            Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MyProfileActivity.this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                }
            },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );
            dpd.show();
        });

        // set language
        editLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, SettingLanguageActivity.class);
            if( SharedPreferencesManager.getLanguage().equals("") )
                intent.putExtra("Language", "English");
            else
                intent.putExtra("Language", SharedPreferencesManager.getLanguage());
            startActivityForResult(intent, REQUEST_LANG);
        });
    }

    private void showEditTextDialog(String message, final MyProfileActivity.EditTextDialogListener listener) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this, R.style.AlertDialogDark);
            final EditText edittext = new EditText(this);
            alert.setMessage(message);
            alert.setView(edittext);
            alert.setNegativeButton(R.string.ok, (dialog, whichButton) -> {
                if (listener != null)
                    listener.onOk(edittext.getText().toString());
            });
            alert.setNeutralButton(R.string.cancel, null);
            alert.show();
        } else {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(this, R.style.AlertDialogLight);
            final EditText edittext = new EditText(this);
            alert.setMessage(message);
            alert.setView(edittext);
            alert.setNegativeButton(R.string.ok, (dialog, whichButton) -> {
                if (listener != null)
                    listener.onOk(edittext.getText().toString());
            });
            alert.setNeutralButton(R.string.cancel, null);
            alert.show();
        }
    }

    private void saveValueToFirebase(String childKey, String value) {
        FireConstants.usersRef.child(Objects.requireNonNull(FireManager.getUid())).child(childKey).setValue(value);
    }

    private interface EditTextDialogListener {
        void onOk(String text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (AppVerUtil.isLogin) {
                if (SharedPreferencesManager.getUserName().isEmpty()) {
                    Toast.makeText(this, R.string.no_name, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getSurname().isEmpty()) {
                    Toast.makeText(this, R.string.no_surname, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getEmail().isEmpty()) {
                    Toast.makeText(this, R.string.no_email, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getBirthday().isEmpty()) {
                    Toast.makeText(this, R.string.no_birth, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getStatus().isEmpty()) {
                    Toast.makeText(this, R.string.no_status, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getGender().isEmpty()) {
                    Toast.makeText(this, R.string.no_gender, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                if (SharedPreferencesManager.getLanguage().isEmpty()) {
                    Toast.makeText(this, R.string.no_lang, Toast.LENGTH_SHORT).show();
                    return super.onOptionsItemSelected(item);
                }
                SharedPreferencesManager.setUserInfoSaved(true);
                Intent intent = new Intent(MyProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File file = DirManager.getMyPhotoPath();
                BitmapUtils.compressImage(resultUri.getPath(), file, 30);
                FireManager.updateMyPhoto(file.getPath(), isSuccessful -> {
                    if (isSuccessful) {
                        try {
                            Glide.with(MyProfileActivity.this)
                                    .load(file)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(imageViewUserProfile);
                            Toast.makeText(MyProfileActivity.this, R.string.image_changed, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LANG) {
            String lang = SharedPreferencesManager.getLanguage();
            tvLanguage.setText(lang);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("language", lang);
            FireConstants.languageRef.child(Objects.requireNonNull(FireManager.getUid())).setValue(hashMap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWithData();
    }

}
