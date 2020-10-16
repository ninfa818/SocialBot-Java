package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.main.MainActivity;
import com.hostcart.socialbot.fragments.ForwardChatFragment;
import com.hostcart.socialbot.fragments.ForwardPostFragment;
import com.hostcart.socialbot.model.ExpandableContact;
import com.hostcart.socialbot.model.Posts;
import com.hostcart.socialbot.model.constants.MessageType;
import com.hostcart.socialbot.model.realms.Message;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.ContactUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.MessageCreator;
import com.hostcart.socialbot.utils.MimeTypes;
import com.hostcart.socialbot.utils.PostMedia;
import com.hostcart.socialbot.utils.PostMediaCreator;
import com.hostcart.socialbot.utils.RealPathUtil;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.Util;
import com.hostcart.socialbot.views.DevlomiSnackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezvcard.VCard;

public class ForwardActivity extends AppCompatActivity {

    public static final int PICK_NUMBERS_REQUEST = 1478;
    
    private Toolbar toolbarForward;
    private FloatingActionButton fabSend;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MenuItem menuItem;
    private DevlomiSnackbar mSnackbar;
    private CoordinatorLayout rootView;

    public List<User> selectedForwardedUsers = new ArrayList<>();

    private ForwardChatFragment chatFragment;
    private ForwardPostFragment postFragment;
    private SearchCallback searchCallback;
    private int pageIndex = 0;
    private int fabStatus = 0;
    private String snackText = "";
    private Posts post = new Posts();


    public interface SearchCallback {
        void onQuery(String newText);
        void onSearchClose();
    }

    public void setSearchCallback(SearchCallback searchCallback) {
        this.searchCallback = searchCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_forward_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_forward_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        initView();

        setSupportActionBar(toolbarForward);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fabSend.show();
        fabSend.setOnClickListener(view -> {
            if (pageIndex == 1) {
                if (isHasIncomingShare()) {
                    if (chatFragment.adapter.getSelectedForwardedUsers().isEmpty())
                        return;
                    handleIncomingShareToChat(chatFragment.adapter.getSelectedForwardedUsers());
                } else {
                    if (chatFragment.adapter.getSelectedForwardedUsers().isEmpty())
                        setResult(RESULT_CANCELED);
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(IntentUtils.EXTRA_DATA_RESULT, (ArrayList<? extends Parcelable>) chatFragment.adapter.getSelectedForwardedUsers());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                if (isHasIncomingShare()) {
                    handleIncomingShareToPost();
                } else { }
            }
        });

        chatFragment.setForwardChatEventListener(new ForwardChatFragment.ForwardChatEventListener() {
            @Override
            public void onShowSnackBarEvent() {
                fabSend.show();
                if (!mSnackbar.isShowing()) {
                    mSnackbar.showSnackBar();
                }

                fabStatus = 1;
            }

            @Override
            public void onHideSnackBarEvent() {
                fabSend.hide();
                mSnackbar.dismissSnackbar();

                fabStatus = 0;
            }

            @Override
            public void onUpdateSelectUser(String text) {
                mSnackbar.getSnackbarTextView().setText(text);

                fabStatus = 1;
                snackText = text;
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                pageIndex = position;
                if (position == 0) {
                    menuItem.setVisible(false);
                    fabSend.show();
                    mSnackbar.dismissSnackbar();
                } else {
                    menuItem.setVisible(true);
                    if (fabStatus == 1) {
                        fabSend.show();
                        mSnackbar.showSnackBar();
                        mSnackbar.getSnackbarTextView().setText(snackText);
                    } else {
                        fabSend.hide();
                        mSnackbar.dismissSnackbar();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private boolean isHasIncomingShare() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        return Intent.ACTION_SEND.equals(action) && type != null || Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null;
    }

    @SuppressLint("SetTextI18n")
    private void handleIncomingShareToPost() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.equals(MimeTypes.TEXT_PLAIN)) {
                Snackbar.make(rootView,"Posting ...",2500).setActionTextColor(Color.GREEN).show();
                handleTextShareToPost();
            } else if (type.startsWith(MimeTypes.IMAGE)) {
                Snackbar.make(rootView,"Posting ...",2500).setActionTextColor(Color.GREEN).show();
                handleImageShareToPost();
            } else if (type.startsWith(MimeTypes.VIDEO)) {
                Snackbar.make(rootView,"Posting ...",2500).setActionTextColor(Color.GREEN).show();
                handleVideoShareToPost();
            } else if (type.startsWith(MimeTypes.CONTACT)) {
                Snackbar.make(rootView,"Wrong post type",2500).setActionTextColor(Color.RED).show();
            } else if (type.startsWith(MimeTypes.AUDIO)) {
                Snackbar.make(rootView,"Wrong post type",2500).setActionTextColor(Color.RED).show();
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleImageShareToPost();
            }
        }
    }

    public HashMap<String,Object> createPostHashMap( String key, String postText, int type, String latlong) {
        setNewPost(key, postText, type, latlong);
        HashMap<String,Object> data = new HashMap<>();
        if(!postText.equals(""))
            data.put("postText",post.getPostText());
        data.put("postId", post.getPostId());
        data.put("postUid", post.getPostUid());
        data.put("postName", post.getPostName());
        data.put("postPhotoUrl", post.getPostPhotoUrl());
        data.put("postShares",post.getPostShares());
        data.put("postIsShared",post.getPostIsShared());
        data.put("postTime", post.getPostTime());
        data.put("postType", post.getPostType());
        if( latlong != null && !latlong.equals("") )
            data.put("postLocation", post.getPostLocation());

        return data;
    }

    private void setNewPost( String key, String postText, int type, String latlong) {
        post.setPostId(key);
        post.setPostUid(FireManager.getUid());
        post.setPostIsShared(false);
        post.setPostShares(0);
        post.setPostText(postText);
        post.setPostName(SharedPreferencesManager.getUserName());
        post.setPostPhotoUrl(SharedPreferencesManager.getThumbImg());
        post.setPostType(type);
        post.setPostTime(new Date().getTime());
        post.setPostIsLiked(false);
        post.setPostLikes(0);
        if( latlong != null && !latlong.equals("") )
            post.setPostLocation(latlong);
    }

    private void handleTextShareToPost() {
        String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null)
            return;
        if (sharedText.contains("http")) {
            if (sharedText.contains("youtu.be")) {
                createYoutubePost(sharedText);
            }
        } else {
            createTextPost(sharedText);
        }
    }

    private void onSuccessPost() {
        Snackbar.make(rootView,"Success Post",1500).setActionTextColor(Color.GREEN).show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ForwardActivity.this, MainActivity.class);
                startTheActivityWithFlags(intent);
                finish();
            }
        }, 2500);
    }

    private void createTextPost( String text ) {
        String key = FireConstants.postsRef.push().getKey();
        HashMap<String,Object> data = createPostHashMap(key, text, 1, null);
        FireConstants.postsRef
                .child(key)
                .updateChildren(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                onSuccessPost();
            } else {
                Snackbar.make(rootView, task.getException().getMessage(), 2500).setActionTextColor(Color.RED).show();
            }
        });
    }

    private void createYoutubePost( String url ) {
        String key = FireConstants.postsRef.push().getKey();
        String text = "";
        if (postFragment != null) {
            text = postFragment.post_text.getText().toString();
        }
        HashMap<String,Object> data = createPostHashMap(key, text, 0, null);
        FireConstants.postsRef
                .child(key)
                .setValue(data).addOnCompleteListener(task -> {
            if( task.isSuccessful() ) {
                saveMediaFromStorage(key, url);
            }
        });
    }

    private void saveMediaFromStorage(String key, String url) {
        PostMedia postMedia = PostMediaCreator.createYoutubePost(url);
        String sub_key = FireConstants.postsRef.child(key).child("posMedias").push().getKey();

        FireConstants.postsRef
                .child(key).child("postMedias").updateChildren(getMediaMap(postMedia, sub_key))
                .addOnCompleteListener(task1 -> {
                    onSuccessPost();
                });
    }

    private Map<String,Object> getMediaMap(PostMedia postMedia, String sKey ) {
        Map<String,Object> ssHashMap = new HashMap<>();
        ssHashMap.put("content", postMedia.getContent());
        ssHashMap.put("duration", postMedia.getDuration());
        ssHashMap.put("localPath", postMedia.getLocalPath());
        ssHashMap.put("thumbImg", postMedia.getThumbImg());
        ssHashMap.put("timestamp", postMedia.getTimestamp());
        ssHashMap.put("type", postMedia.getType());
        ssHashMap.put("userId", postMedia.getUserId());

        Map<String,Object> sHashMap = new HashMap<>();
        sHashMap.put(sKey, ssHashMap);

        return  sHashMap;
    }

    private void handleImageShareToPost() {
    }

    private void handleVideoShareToPost() {
    }

    private void handleIncomingShareToChat(List<User> selectedUsers) {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.equals(MimeTypes.TEXT_PLAIN)) {
                handleTextShare(selectedUsers);
            } else if (type.startsWith(MimeTypes.IMAGE)) {
                handleImageShare(selectedUsers);
            } else if (type.startsWith(MimeTypes.VIDEO)) {
                handleVideoShare();
            } else if (type.startsWith(MimeTypes.CONTACT)) {
                Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                List<VCard> vcards = ContactUtils.getContactAsVcard(this, uri);
                List<ExpandableContact> contactNameList = ContactUtils.getContactNamesFromVcard(vcards);
                Intent mIntent = new Intent(this, SelectContactNumbersActivity.class);
                mIntent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) contactNameList);
                startActivityForResult(mIntent, PICK_NUMBERS_REQUEST);
            } else if (type.startsWith(MimeTypes.AUDIO)) {
                handleIncomingAudio();
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleImageShare(chatFragment.adapter.getSelectedForwardedUsers());
            }
        }
    }

    private void handleIncomingAudio() {
        Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        String filePath = RealPathUtil.getRealPath(this, uri);
        if (filePath == null) {
            showFileNotFoundToast();
            return;
        }
        String length = Util.getVideoLength(this, filePath);

        if (chatFragment.adapter.getSelectedForwardedUsers().size() > 1) {
            for (User user : chatFragment.adapter.getSelectedForwardedUsers()) {
                Message message = new MessageCreator.Builder(user, MessageType.SENT_AUDIO).path(filePath).duration(length).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            }
            finish();
        } else {
            User user = chatFragment.adapter.getSelectedForwardedUsers().get(0);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.AUDIO);
            intent.putExtra(IntentUtils.UID, user.getUid());
            startTheActivityWithFlags(intent);
            finish();
        }
    }

    private void handleVideoShare() {
        Uri videoUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        String filePath = RealPathUtil.getRealPath(this, videoUri);
        if (filePath == null) {
            showFileNotFoundToast();
            return;
        }
        if (chatFragment.adapter.getSelectedForwardedUsers().size() > 1) {
            for (User user : chatFragment.adapter.getSelectedForwardedUsers()) {
                Message message = new MessageCreator.Builder(user, MessageType.SENT_VIDEO).context(this).path(filePath).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                showSendingToast();
                finish();
            }
        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            User user = chatFragment.adapter.getSelectedForwardedUsers().get(0);
            intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.VIDEO);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startTheActivityWithFlags(intent);
            finish();
        }
    }

    private void handleImageShare(List<User> selectedUsers) {
        ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            if (selectedUsers.size() > 1) {
                for (User user : selectedUsers) {
                    for (Uri uri : imageUris) {
                        String filePath = RealPathUtil.getRealPath(this, uri);
                        if (filePath != null) {
                            Message message = new MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath).fromCamera(false).build();
                            ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                        } else {
                            showFileNotFoundToast();
                        }
                    }
                }
                finish();
            } else {
                ArrayList<String> realPathList = (ArrayList<String>) getRealPathList(imageUris);
                User user = selectedUsers.get(0);
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH_LIST, realPathList);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startTheActivityWithFlags(intent);
                finish();
            }
        } else {
            Uri imageUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            String filePath = RealPathUtil.getRealPath(this, imageUri);
            if (filePath == null) {
                showFileNotFoundToast();
                return;
            }
            if (selectedUsers.size() > 1) {
                for (User user : selectedUsers) {
                    Message message = new MessageCreator.Builder(user, MessageType.SENT_IMAGE).path(filePath).fromCamera(false).build();
                    ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                }
                finish();
            } else {
                User user = selectedUsers.get(0);
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(IntentUtils.EXTRA_REAL_PATH, filePath);
                intent.putExtra(IntentUtils.UID, user.getUid());
                intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.IMAGE);
                startTheActivityWithFlags(intent);
                finish();
            }
        }
    }

    private void handleTextShare(List<User> selectedUsers) {
        String sharedText = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null)
            return;
        if (selectedUsers.size() > 1 || sharedText.contains("MEETING ID - ")) {
            for (User selectedUser : selectedUsers) {
                Message message = new MessageCreator.Builder(selectedUser, MessageType.SENT_TEXT).text(sharedText).build();
                ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
            }
            showSendingToast();
            finish();
        } else {
            User user = selectedUsers.get(0);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(IntentUtils.EXTRA_SHARED_TEXT, sharedText);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.TEXT_PLAIN);
            startTheActivityWithFlags(intent);
            finish();
        }
    }

    private void handleContacts(List<ExpandableContact> selectedContacts) {
        if (chatFragment.adapter.getSelectedForwardedUsers().size() > 1) {
            for (User user : chatFragment.adapter.getSelectedForwardedUsers()) {
                List<Message> messages = new MessageCreator.Builder(user, MessageType.SENT_CONTACT).contacts(selectedContacts).buildContacts();
                for (Message message : messages) {
                    ServiceHelper.startNetworkRequest(this, message.getMessageId(), message.getChatId());
                }
            }
            showSendingToast();
        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST, (ArrayList<? extends Parcelable>) selectedContacts);
            User user = chatFragment.adapter.getSelectedForwardedUsers().get(0);
            intent.putExtra(IntentUtils.UID, user.getUid());
            intent.putExtra(IntentUtils.EXTRA_MIME_TYPE, MimeTypes.CONTACT);
            startTheActivityWithFlags(intent);
            finish();
        }
    }

    private void showSendingToast() {
        Toast.makeText(this, R.string.sending_messages, Toast.LENGTH_SHORT).show();
    }

    private void showFileNotFoundToast() {
        Toast.makeText(this, R.string.could_not_get_this_file, Toast.LENGTH_SHORT).show();
    }

    private List<String> getRealPathList(ArrayList<Uri> imageUris) {
        List<String> realPathList = new ArrayList<>();
        for (Uri uri : imageUris) {
            realPathList.add(RealPathUtil.getRealPath(this, uri));
        }
        return realPathList;
    }

    private void startTheActivityWithFlags(Intent intent) {
        TaskStackBuilder sBuilder = TaskStackBuilder.create(this);
        sBuilder.addNextIntentWithParentStack(new Intent(this, MainActivity.class));
        sBuilder.addNextIntent(intent);
        sBuilder.startActivities();
    }

    private void initView() {
        chatFragment = new ForwardChatFragment(this);
        postFragment = new ForwardPostFragment();

        toolbarForward = findViewById(R.id.toolbar_forward);
        rootView = findViewById(R.id.root_view);
        mSnackbar = new DevlomiSnackbar(rootView, getColor(R.color.blue));
        fabSend = findViewById(R.id.fab_send);

        tabLayout = findViewById(R.id.tap_share);
        viewPager = findViewById(R.id.vpg_share);
        tabLayout.post(() -> tabLayout.setupWithViewPager(viewPager));
        viewPager.setAdapter(new ForwardShareAdapter(getSupportFragmentManager()));
    }

    class ForwardShareAdapter extends FragmentPagerAdapter {

        private String[] str_titles = {"Share to Post ..", "Share to Chat .."};

        ForwardShareAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NotNull
        public Fragment getItem(int position) {
            if (position == 0) {
                return postFragment;
            } else {
                return chatFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return str_titles[position];
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_forward_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_forward_light, menu);
        }

        menuItem = menu.findItem(R.id.menu_item_search);
        menuItem.setVisible(false);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchCallback != null)
                    searchCallback.onQuery(newText);
                return false;
            }

        });

        searchView.setOnCloseListener(() -> {
            searchCallback.onSearchClose();
            return false;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_NUMBERS_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<ExpandableContact> selectedContacts = data.getParcelableArrayListExtra(IntentUtils.EXTRA_CONTACT_LIST);
                handleContacts(selectedContacts);
            } else {
                Toast.makeText(this, R.string.not_contact_selected, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        setResult(RESULT_CANCELED);
        super.onDestroy();
    }

}
