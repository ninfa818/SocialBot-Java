package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.events.UserImageDownloadedEvent;
import com.hostcart.socialbot.model.constants.GroupEventTypes;
import com.hostcart.socialbot.model.realms.GroupEvent;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.CropImageRequest;
import com.hostcart.socialbot.utils.DirManager;
import com.hostcart.socialbot.utils.FileUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.GroupManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

public class ProfilePhotoActivity extends AppCompatActivity {

    private ImageView profileFullScreen;

    User user;
    FireManager.OnUpdateUserPhoto onUpdateUserPhoto;
    String profilePhotoPath;
    private int IMAGE_QUALITY_COMPRESS = 30;
    private boolean isGroup = false;
    private boolean isBroadcast = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_profile_photo_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_profile_photo_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        Toolbar toolbarProfile = findViewById(R.id.toolbar_profile);
        profileFullScreen = findViewById(R.id.profile_full_screen);

        setSupportActionBar(toolbarProfile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().hasExtra(IntentUtils.UID)) {
            String uid = getIntent().getStringExtra(IntentUtils.UID);
            user = RealmHelper.getInstance().getUser(uid);
            isBroadcast = user.isBroadcastBool();
            isGroup = user.isGroupBool();
            profilePhotoPath = user.getUserLocalPhoto();
            getSupportActionBar().setTitle(user.getUserName());
        } else {
            String imgPath = getIntent().getStringExtra(IntentUtils.EXTRA_PROFILE_PATH);
            getSupportActionBar().setTitle(R.string.profile_photo);
            Glide.with(this).load(imgPath)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(profileFullScreen);
        }
    }

    private void loadImage(final String profilePhotoPath) {
        if (user == null) return;
        if (isBroadcast) {
            Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.ic_broadcast_with_bg);
            profileFullScreen.setImageDrawable(drawable);
            //if the profilePhotoPath in Database is not exists
        } else if (profilePhotoPath == null) {
            //show the thumgImg while getting full Image
            if (user.getThumbImg() != null) {
                Glide.with(this).asBitmap().load(BitmapUtils.encodeImageAsBytes(user.getThumbImg())).into(profileFullScreen);
            }
            //start getting full image
            FireManager.downloadUserPhoto(user.getUid(), user.getUserLocalPhoto(), isGroup, onUpdateUserPhoto);
        } else {
            //otherwise check if the image stored in device
            //if it's stored then show it
            if (FileUtils.isFileExists(profilePhotoPath)) {
                Glide.with(this).load(profilePhotoPath).into(profileFullScreen);
            } else {
                //otherwise download the image
                FireManager.downloadUserPhoto(user.getUid(), user.getUserLocalPhoto(), isGroup, onUpdateUserPhoto);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_profile_photo_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_profile_photo_light, menu);
        }
        if (isGroup && FireManager.isAdmin(user.getGroup().getAdminsUids()) || !getIntent().hasExtra(IntentUtils.UID)) {
            menu.findItem(R.id.edit_profile_item).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.edit_profile_item) {
            editProfilePhoto();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onUpdateUserPhoto = photoPath -> {
            try {
                //load the image once it's downloaded
                Glide.with(ProfilePhotoActivity.this).load(photoPath)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(profileFullScreen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        loadImage(profilePhotoPath);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //free up resources and avoid memory leaks
        onUpdateUserPhoto = null;
    }

    private void editProfilePhoto() {
        pickImages();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final File file = DirManager.generateUserProfileImage();

                //it is not recommended to change IMAGE_QUALITY_COMPRESS as it may become
                //too big and this may cause the app to crash due to large thumbImg
                //therefore the thumb img may became un-parcelable through activities
                BitmapUtils.compressImage(resultUri.getPath(), file, IMAGE_QUALITY_COMPRESS);

                if (isGroup) {
                    GroupManager.changeGroupImage(file.getPath(), user.getUid(), isSuccessful -> {
                        if (isSuccessful) {

                            try {
                                Glide.with(ProfilePhotoActivity.this)
                                        .load(file)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(profileFullScreen);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                            new GroupEvent(SharedPreferencesManager.getPhoneNumber(), GroupEventTypes.GROUP_SETTINGS_CHANGED, null).createGroupEvent(user, null);

                            Toast.makeText(ProfilePhotoActivity.this, R.string.image_changed, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    FireManager.updateMyPhoto(file.getPath(), isSuccessful -> {
                        if (isSuccessful) {
                            //skip cache because the img name will still the same
                            //and glide will think this is same image,therefore it
                            //will still show the old image
                            try {
                                Glide.with(ProfilePhotoActivity.this)
                                        .load(file)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(profileFullScreen);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
//
                            Toast.makeText(ProfilePhotoActivity.this, R.string.image_changed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, R.string.could_not_get_this_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(this);
    }

    //load the image if it's downloaded by previous activity or service
    @Subscribe
    public void userImageDownloaded(UserImageDownloadedEvent event) {
        String imagePath = event.getPath();
        Glide.with(this).load(imagePath).into(profileFullScreen);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}
