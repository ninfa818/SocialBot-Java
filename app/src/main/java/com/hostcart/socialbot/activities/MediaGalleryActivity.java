package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.MediaGalleryAdapter;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.dialogs.DeleteDialog;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.MessageCreator;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.views.GridItemDecoration;

import java.util.List;

public class MediaGalleryActivity extends AppCompatActivity {

    private static int SPAN_COUNT = 3;
    private static int SPACING = 16;
    private static final int ITEMS_COUNT_PER_ROW = 3;
    private static final int REQUEST_FORWARD = 145;

    private Toolbar toolbar;
    private RecyclerView rvMediaGallery;
    public boolean isInActionMode = false;
    MediaGalleryAdapter adapter;
    private TextView tvSelectedImagesCount;
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_media_gallery_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_media_gallery_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        init();
        setSupportActionBar(toolbar);

        String uid = getIntent().getStringExtra(IntentUtils.UID);
        user = RealmHelper.getInstance().getUser(uid);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(user.getUserName());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        List<Message> mediaInChat = RealmHelper.getInstance().getMediaInChat(user.getUid());

        adapter = new MediaGalleryAdapter(this, mediaInChat);
        rvMediaGallery.setLayoutManager(new GridLayoutManager(this, ITEMS_COUNT_PER_ROW));
        rvMediaGallery.addItemDecoration(new GridItemDecoration(SPAN_COUNT, SPACING, false));
        rvMediaGallery.setAdapter(adapter);
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar_gallery);
        rvMediaGallery = findViewById(R.id.rv_media_gallery);
        tvSelectedImagesCount = findViewById(R.id.tv_selected_images_count);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_item_forward:
                forwardItemClicked();
                break;
            case R.id.menu_item_delete:
                deleteItemClicked();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteItemClicked() {
        DeleteDialog deleteDialog = new DeleteDialog(this, true);
        deleteDialog.setmListener(isDeleteChecked -> {
            adapter.deleteItems(isDeleteChecked);
            exitActionMode();
        });
        deleteDialog.show();
    }

    private void forwardItemClicked() {
        Intent intent = new Intent(this, ForwardActivity.class);
        startActivityForResult(intent, REQUEST_FORWARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FORWARD && resultCode == RESULT_OK) {
            //get selected users
            List<User> pickedUsers = (List<User>) data.getSerializableExtra(IntentUtils.EXTRA_DATA_RESULT);

            //if the user selects only one user to send the images to him
            //then send the images and the launch activity with that user
            if (pickedUsers != null) {
                if (pickedUsers.size() == 1) {
                    for (Message message : adapter.getSelectedItems()) {
                        Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                        ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), message.getChatId());
                    }
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra(IntentUtils.UID, user.getUid());
                    startActivity(intent);
                    finish();
                    //otherwise send the images to the users and finish this activity
                } else {
                    for (User pickedUser : pickedUsers) {
                        for (Message message : adapter.getSelectedItems()) {
                            Message forwardedMessage = MessageCreator.createForwardedMessage(message, pickedUser, FireManager.getUid());
                            ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), message.getChatId());
                        }
                    }

                    Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    public void addItemToActionMode(int itemsCount) {
        tvSelectedImagesCount.setText(itemsCount + "");
    }

    public void onActionModeStarted() {
        if (!isInActionMode) {
            toolbar.getMenu().clear();
            if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
                toolbar.inflateMenu(R.menu.menu_gallery_action_dark);
            } else {
                toolbar.inflateMenu(R.menu.menu_gallery_action_light);
            }
            setToolbarTitle(false);
        }

        isInActionMode = true;
        tvSelectedImagesCount.setVisibility(View.VISIBLE);
    }

    public void exitActionMode() {
        adapter.exitActionMode();
        isInActionMode = false;
        tvSelectedImagesCount.setVisibility(View.GONE);
        toolbar.getMenu().clear();
        setToolbarTitle(true);
    }

    public boolean isInActionMode() {
        return isInActionMode;
    }

    @Override
    public void onBackPressed() {
        if (isInActionMode)
            exitActionMode();
        else
            super.onBackPressed();
    }

    private void setToolbarTitle(boolean setVisible) {
        if (getSupportActionBar() != null) {
            if (setVisible)
                getSupportActionBar().setTitle(user.getUserName());
            else
                getSupportActionBar().setTitle("");
        }
    }

    public User getUser() {
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //update items if items deleted
        adapter.notifyDataSetChanged();
    }

}
