package com.hostcart.socialbot.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.UsersAdapter;
import com.hostcart.socialbot.events.SyncContactsFinishedEvent;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.ServiceHelper;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.realm.RealmResults;

public class NewChatActivity extends AppCompatActivity implements UsersAdapter.OnItemClickListener {
    private RecyclerView rvNewChat;
    UsersAdapter adapter;
    RealmResults<User> userList;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private ImageButton refreshContactsBtn;
    private LinearLayout noContactsLayout;
    private LinearLayout recyclerContainer;
    private TextView tvNoContacts;
    private Button btnInvite;
    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_new_chat_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_new_chat_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        init();
        setSupportActionBar(toolbar);
        userList = getListOfUsers();
        setTheAdapter();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.select_contact);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadAd();
        updateNoContactsLayout();

        refreshContactsBtn.setOnClickListener(view -> syncContacts());

        btnInvite.setOnClickListener(v -> startActivity(IntentUtils.getShareAppIntent(NewChatActivity.this)));
    }

    private void loadAd() {
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                adView.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
            }
        });

        if (getResources().getBoolean(R.bool.is_new_chat_ad_enabled))
            adView.loadAd(new AdRequest.Builder().build());
    }

    private void syncContacts() {
        progressBar.setVisibility(View.VISIBLE);
        refreshContactsBtn.setVisibility(View.GONE);
        ServiceHelper.startSyncContacts(this);
    }

    private void init() {
        rvNewChat = findViewById(R.id.rv_new_chat);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar_sync);
        adView = findViewById(R.id.ad_view);
        refreshContactsBtn = findViewById(R.id.refresh_contacts_btn);
        noContactsLayout = findViewById(R.id.no_contacts_container);
        recyclerContainer = findViewById(R.id.recycler_container);
        btnInvite = findViewById(R.id.btn_invite);
        tvNoContacts = findViewById(R.id.tv_no_contacts);
    }

    private RealmResults<User> getListOfUsers() {
        return RealmHelper.getInstance().getListOfUsers();
    }

    private void setTheAdapter() {
        adapter = new UsersAdapter(userList, true, this);
        rvNewChat.setLayoutManager(new LinearLayoutManager(this));
        rvNewChat.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.invite_item) {
            startActivity(IntentUtils.getShareAppIntent(NewChatActivity.this));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_new_chat_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_new_chat_light, menu);
        }
        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, false);
                    adapter = new UsersAdapter(users, true, NewChatActivity.this);
                    rvNewChat.setAdapter(adapter);
                } else {
                    adapter = new UsersAdapter(userList, true, NewChatActivity.this);
                    rvNewChat.setAdapter(adapter);
                }
                return false;
            }

        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter = new UsersAdapter(userList, true, NewChatActivity.this);
                rvNewChat.setAdapter(adapter);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //hide progress bar when sync contacts finishes
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncFinished(SyncContactsFinishedEvent event) {
        progressBar.setVisibility(View.GONE);
        refreshContactsBtn.setVisibility(View.VISIBLE);
        updateNoContactsLayout();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(User user) {
        Intent intent = new Intent(NewChatActivity.this, ChatActivity.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        startActivity(intent);
        finish();
    }

    @Override
    public void onUserPhotoClick(User user) {
        Intent intent = new Intent(NewChatActivity.this, ProfilePhotoDialog.class);
        intent.putExtra(IntentUtils.UID, user.getUid());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void updateNoContactsLayout() {
        if (userList == null) return;
        if (userList.isEmpty()) {
            String noContactsText = String.format(getString(R.string.no_contacts), getString(R.string.app_name));
            tvNoContacts.setText(noContactsText);
            noContactsLayout.setVisibility(View.VISIBLE);
            recyclerContainer.setVisibility(View.GONE);
        } else {
            noContactsLayout.setVisibility(View.GONE);
            recyclerContainer.setVisibility(View.VISIBLE);
        }
    }

}
