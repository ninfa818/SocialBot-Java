package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.SharedElementCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.FullScreenAdapter;
import com.hostcart.socialbot.interfaces.ToolbarStateChange;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.ListUtil;
import com.hostcart.socialbot.utils.MessageCreator;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.TimeHelper;
import com.hostcart.socialbot.views.dialogs.DeleteDialog;

import java.util.List;
import java.util.Map;

import io.realm.RealmList;
import me.zhanghai.android.systemuihelper.SystemUiHelper;

import static com.hostcart.socialbot.utils.IntentUtils.EXTRA_CURRENT_ALBUM_POSITION;
import static com.hostcart.socialbot.utils.IntentUtils.EXTRA_CURRENT_MESSAGE_ID;
import static com.hostcart.socialbot.utils.IntentUtils.EXTRA_FIRST_VISIBLE_ITEM_POSITION;
import static com.hostcart.socialbot.utils.IntentUtils.EXTRA_LAST_VISIBLE_ITEM_POSITION;
import static com.hostcart.socialbot.utils.IntentUtils.EXTRA_STARTING_POSITION;


public class FullscreenActivity extends AppCompatActivity implements ToolbarStateChange {

    private static final int REQUEST_CODE_FORWARD = 145;
    public static final int TOOLBAR_ANIMATION_DURATION = 400;

    Toolbar toolbar;
    private ViewPager viewPager;
    private TextView toolbarName, toolbarTime;
    private LinearLayout appbarWrapper;
    private FrameLayout appbar;

    //only media from this chat
    List<Message> images;
    //get whole chat list
    List<Message> messages;

    FullScreenAdapter adapter;
    SystemUiHelper helper;
    User user;

    int mCurrentPosition = 0;
    private int mStartingPosition;
    private int mFirstVisibleItem;
    private int mLastVisibleItem;

    private boolean mIsReturning;


    //transition
    private final SharedElementCallback mCallback = new SharedElementCallback() {
        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
        if (mIsReturning) {
            ImageView sharedElement = (ImageView) adapter.getImage();
            if (sharedElement == null) {
                names.clear();
                sharedElements.clear();
            } else if (mStartingPosition != mCurrentPosition) {
                int realListPosition = Message.getPosFromId(images.get(mCurrentPosition).getMessageId(), messages);

                names.clear();
                sharedElements.clear();

                if (realListPosition <= mFirstVisibleItem && realListPosition <= mLastVisibleItem) {
                    names.add(ViewCompat.getTransitionName(sharedElement));
                    sharedElements.put(ViewCompat.getTransitionName(sharedElement), sharedElement);
                }
            }
        }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_fullscreen_dark);
        } else {
            setContentView(R.layout.activity_fullscreen_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        init();

        //transitions
        supportPostponeEnterTransition();
        setEnterSharedElementCallback(mCallback);

        mFirstVisibleItem = getIntent().getIntExtra(EXTRA_FIRST_VISIBLE_ITEM_POSITION, 0);
        mLastVisibleItem = getIntent().getIntExtra(EXTRA_LAST_VISIBLE_ITEM_POSITION, 0);

        setSupportActionBar(toolbar);

        //hide or show toolbar when the bar state is changed
        helper = new SystemUiHelper(this, SystemUiHelper.LEVEL_IMMERSIVE, SystemUiHelper.FLAG_IMMERSIVE_STICKY, this::animateToolbar);

        helper.show();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String selectedImgMessageId = getIntent().getStringExtra(IntentUtils.EXTRA_MESSAGE_ID);

        String uid = getIntent().getStringExtra(IntentUtils.UID);
        user = RealmHelper.getInstance().getUser(uid);
        String chatId = user.getUid();

        images = RealmHelper.getInstance().getMediaInChat(chatId);
        messages = RealmHelper.getInstance().getMessagesInChat(chatId);

        mCurrentPosition = getPosFromId(selectedImgMessageId);

        mStartingPosition = mCurrentPosition;

        adapter = new FullScreenAdapter(getSupportFragmentManager(), this, images, mCurrentPosition);

        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(mCurrentPosition);
        setToolbarData(images.get(mCurrentPosition));

        //onSwipe
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                setToolbarData(images.get(position));
            }

        });
    }

    private void animateToolbar(boolean visible) {
        if (visible) {
            appbarWrapper.animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(TOOLBAR_ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        } else {
            appbarWrapper.animate()
                    .alpha(0)
                    .translationY(-appbar.getBottom())
                    .setDuration(TOOLBAR_ANIMATION_DURATION)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
        }
    }

    private void init() {
        toolbar = findViewById(R.id.toolbar);
        toolbarName = findViewById(R.id.toolbar_name);
        toolbarTime = findViewById(R.id.toolbar_time);
        appbarWrapper = findViewById(R.id.appbar_wrapper);
        appbar = findViewById(R.id.appbar);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_img_activity_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_img_activity_light, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finishAfterTransition() {
        mIsReturning = true;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_POSITION, mStartingPosition);

        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        String messageId = images.get(mCurrentPosition).getMessageId();
        data.putExtra(EXTRA_CURRENT_MESSAGE_ID, messageId);

        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.forward_item:
                forwardItemClicked();
                break;

            case R.id.delete_item:
                deleteItemClicked();
                break;

            case R.id.menu_item_share:
                shareItemClicked();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareItemClicked() {
        String path = images.get(mCurrentPosition).getLocalPath();
        Intent shareImageIntent = IntentUtils.getShareImageIntent(path);
        shareImageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareImageIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(shareImageIntent);
    }

    private void deleteItemClicked() {
        DeleteDialog deleteDialog = new DeleteDialog(this, true);

        deleteDialog.setmListener(isDeleteChecked -> {
            String messageId = images.get(mCurrentPosition).getMessageId();
            String chatId = images.get(mCurrentPosition).getChatId();

            RealmHelper.getInstance().deleteMessageFromRealm(chatId, messageId, isDeleteChecked);
            adapter.notifyDataSetChanged();
            if (images.isEmpty())
                finish();
        });

        deleteDialog.show();
    }


    private void forwardItemClicked() {
        Intent intent = new Intent(this, ForwardActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FORWARD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FORWARD && resultCode == RESULT_OK) {
            List<User> selectedList = data.getParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT);
            if (selectedList == null || selectedList.isEmpty())
                return;

            Message message = images.get(mCurrentPosition);

            if (selectedList.size() == 1) {
                Intent intent = new Intent(this, ChatActivity.class);
                User user = selectedList.get(0);
                Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                intent.putExtra(IntentUtils.EXTRA_FORWARDED, true);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MESSAGE, forwardedMessage);

                //prevent duplicate
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                for (User user : selectedList) {
                    Message forwardedMessage = MessageCreator.createForwardedMessage(message, user, FireManager.getUid());
                    ServiceHelper.startNetworkRequest(this, forwardedMessage.getMessageId(), forwardedMessage.getChatId());
                }
                Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setToolbarData(Message message) {
        toolbarName.setText(getSenderName(message.getFromId()));
        toolbarTime.setText(TimeHelper.getMediaTime(Long.parseLong(message.getTimestamp())));
    }

    private String getSenderName(String fromId) {
        if (fromId.equals(FireManager.getUid())) {
            return getString(R.string.you);
        }

        if (user.isGroupBool()) {
            RealmList<User> users = user.getGroup().getUsers();
            User userById = ListUtil.getUserById(fromId, users);
            if (userById != null)
                return userById.getUserName();
        }

        return user.getUserName();
    }

    private int getPosFromId(String messageId) {
        for (int i = 0; i < images.size(); i++) {
            Message message = images.get(i);
            if (message.getMessageId().equals(messageId)) {
                return i;
            }
        }
        return 0;
    }


    @Override
    public void hideToolbar() {
        helper.hide();
    }

    @Override
    public void showToolbar() {
        helper.show();
    }

    @Override
    public void toggle() {
        helper.toggle();
    }

}
