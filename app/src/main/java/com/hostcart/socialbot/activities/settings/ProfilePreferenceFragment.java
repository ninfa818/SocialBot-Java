package com.hostcart.socialbot.activities.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.AddedMeActivity;
import com.hostcart.socialbot.activities.FriendsActivity;
import com.hostcart.socialbot.activities.ProfilePhotoActivity;
import com.hostcart.socialbot.activities.QuickAddActivity;
import com.hostcart.socialbot.activities.SettingLanguageActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static com.hostcart.socialbot.activities.MyProfileActivity.REQUEST_LANG;

/**
 * Created by Devlomi on 25/03/2018.
 */

public class ProfilePreferenceFragment extends PreferenceFragment {

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

    private List<UserInfo> userList;
    private List<UserInfo> remainList;
    private List<UserInfo> addedList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickImages() {
        CropImageRequest.getCropImageRequest().start(getActivity(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            view = inflater.inflate(R.layout.fragment_profile_settings_dark, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_profile_settings_light, container, false);
        }

        imageViewUserProfile = view.findViewById(R.id.image_view_user_profile);
        changeUserProfile = view.findViewById(R.id.image_button_change_user_profile);
        tvUsername = view.findViewById(R.id.tv_username);
        tvSurname = view.findViewById(R.id.tv_surname);
        editUsername = view.findViewById(R.id.image_button_edit_username);
        editSurname = view.findViewById(R.id.image_button_edit_surname);
        editEmail = view.findViewById(R.id.edit_email);
        editGender = view.findViewById(R.id.edit_gender);
        editStatus = view.findViewById(R.id.edit_status);
        editLanguage = view.findViewById(R.id.edit_language);
        editBirth = view.findViewById(R.id.edit_birth);

        tvStatus = view.findViewById(R.id.tv_status);
        tvEmail = view.findViewById(R.id.tv_email);
        tvGender = view.findViewById(R.id.tv_gender);
        TextView tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvLanguage = view.findViewById(R.id.tv_Language);
        tvBirth = view.findViewById(R.id.tv_birth);

        String userName = SharedPreferencesManager.getUserName();
        String status = SharedPreferencesManager.getStatus();
        String phoneNumber = SharedPreferencesManager.getPhoneNumber();
        final String myPhoto = SharedPreferencesManager.getMyPhoto();
        tvStatus.setText(status);
        tvUsername.setText(userName);
        tvPhoneNumber.setText(phoneNumber);
        //
        tvSurname.setText(SharedPreferencesManager.getSurname());
        tvEmail.setText(SharedPreferencesManager.getEmail());
        tvGender.setText(SharedPreferencesManager.getGender());

        tvLanguage.setText(SharedPreferencesManager.getLanguage());

        String photoUri = SharedPreferencesManager.getMyPhoto();

        // init click listener
        initClickGroup(myPhoto);

        Glide.with(getActivity())
                .asBitmap()
                .load(Uri.parse(photoUri))
                .into(imageViewUserProfile);

        Button friendsButton = view.findViewById(R.id.friends_button);
        friendsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendsActivity.class);
            startActivity(intent);
        });

        Button addedButton = view.findViewById(R.id.added_button);
        addedButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddedMeActivity.class);
            startActivity(intent);
        });
        loadInvitedList();
        if( addedList != null && addedList.size() > 0 )
            addedButton.setText(String.format(Locale.US, "Added Me (%d)", addedList.size()));
        else
            addedButton.setEnabled(false);

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), QuickAddActivity.class);
            startActivity(intent);
        });
        loadRemainList();
        if( remainList != null && remainList.size() > 0 )
            addButton.setText(String.format(Locale.US, "Quick Add (%d)", remainList.size()));
        else
            addButton.setEnabled(false);

        loadFriendListJsonString();
        if( userList == null )
            friendsButton.setEnabled(false);
        else
            friendsButton.setText(String.format(Locale.US, "Friends (%d)", userList.size()));

        return view;
    }

    private void initClickGroup(final String photo) {
        imageViewUserProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfilePhotoActivity.class);
            String transName = "profile_photo_trans";

            intent.putExtra(IntentUtils.EXTRA_PROFILE_PATH, photo);
            startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, transName).toBundle());
        });

        // change picture image
        changeUserProfile.setOnClickListener(v -> pickImages());

        // full name
        editUsername.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_name), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.username_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(getActivity())) {
                FireManager.updateMyUserName(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveMyUsername(text);
                        saveValueToFirebase("name", text);
                        tvUsername.setText(text);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                    }
                });

            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // surname
        editSurname.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_surname), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.surname_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(getActivity())) {
                FireManager.updateMySurname(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveSurname(text);
                        tvSurname.setText(text);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                    }
                });

            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // status
        editStatus.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_status), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.status_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(getActivity())) {
                FireManager.updateMyStatus(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveMyStatus(text);
                        saveValueToFirebase("status", text);
                        tvStatus.setText(text);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();

                    }
                });

            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // email
        editEmail.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_email), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.email_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(getActivity())) {
                FireManager.updateMyEmail(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveEmail(text);
                        tvEmail.setText(text);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // Gender
        editGender.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_gender), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.gender_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferencesManager.saveGender(text);
            tvGender.setText(text);
        }));

        // set birthday
        editBirth.setOnClickListener(v -> showEditTextDialog(getString(R.string.enter_your_birthdate), text -> {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(getActivity(), R.string.birthdate_is_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkHelper.isConnected(getActivity())) {
                FireManager.updateMyBirthday(text, isSuccessful -> {
                    if (isSuccessful) {
                        SharedPreferencesManager.saveBirthday(text);
                        tvBirth.setText(text);
                    } else {
                        Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            }
        }));

        // set language
        editLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingLanguageActivity.class);
            intent.putExtra("Language", SharedPreferencesManager.getLanguage());
            startActivityForResult(intent, REQUEST_LANG);
        });
    }

    private void showEditTextDialog(String message, final EditTextDialogListener listener) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogDark);
            final EditText edittext = new EditText(getActivity());
            alert.setMessage(message);
            alert.setView(edittext);
            alert.setNegativeButton(R.string.ok, (dialog, whichButton) -> {
                if (listener != null)
                    listener.onOk(edittext.getText().toString());
            });
            alert.setNeutralButton(R.string.cancel, null);
            alert.show();
        } else {
            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getActivity(), R.style.AlertDialogLight);
            final EditText edittext = new EditText(getActivity());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final File file = DirManager.getMyPhotoPath();
                BitmapUtils.compressImage(resultUri.getPath(), file, 30);

                FireManager.updateMyPhoto(file.getPath(), isSuccessful -> {
                    if (isSuccessful) {
                        try {
                            Glide.with(getActivity())
                                    .load(file)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .into(imageViewUserProfile);
                            Toast.makeText(getActivity(), R.string.image_changed, Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else if( requestCode == REQUEST_LANG ) {
            String lang = SharedPreferencesManager.getLanguage();

            tvLanguage.setText(lang);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("language", lang);
            FireConstants.languageRef.child(Objects.requireNonNull(FireManager.getUid())).setValue(hashMap);
        }
    }


    private void loadInvitedList() {
        String jsonstring = SharedPreferencesManager.getAddedMeListJsonString();
        if(jsonstring.equals(""))
            return;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        addedList = gson.fromJson(jsonstring, token.getType());
    }

    private void loadRemainList() {
        String jsonstring = SharedPreferencesManager.getRemainListJsonString();
        if(jsonstring.equals(""))
            return;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        remainList = gson.fromJson(jsonstring, token.getType());
    }

    private void loadFriendListJsonString() {
        String jsonstring = SharedPreferencesManager.getFriendsListJsonString();
        if(jsonstring.equals(""))
            return;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        userList = gson.fromJson(jsonstring, token.getType());
    }

}

