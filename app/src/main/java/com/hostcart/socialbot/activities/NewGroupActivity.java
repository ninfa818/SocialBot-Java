package com.hostcart.socialbot.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.hidely.hidelyviews.HidelyImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.ForwardAdapter;
import com.hostcart.socialbot.adapters.NewGroupAdapter;
import com.hostcart.socialbot.adapters.NewGroupSelectedUsersAdapter;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BroadcastManager;
import com.hostcart.socialbot.utils.GroupManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.NetworkHelper;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.views.dialogs.SetGroupTitleDialog;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

public class NewGroupActivity extends ForwardActivity implements ForwardAdapter.OnUserClick, NewGroupSelectedUsersAdapter.OnUserClick, ForwardActivity.SearchCallback {
    private static final int MAX_GROUP_USERS_COUNT = 50;
    private static final int MAX_BROADCAST_USERS_COUNT = 100;
    private int EXTRA_COUNT = 0;
    private RecyclerView rvSelectedUsersNewGroup;
    private RecyclerView rvGroup;
    private FloatingActionButton fabNext;

    NewGroupAdapter allUsersAdapter;
    NewGroupSelectedUsersAdapter selectedUsersAdapter;
    RealmResults<User> users;

    List<User> selectedUsers;
    List<User> currentUsers;
    private TextView tvAddParticipantsTvToolbar;
    private TextView toolbarTitle;
    private boolean isBroadcast;


    @Override
    public void onQuery(String newText) {
        if (!newText.trim().isEmpty()) {
            RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, false);
            allUsersAdapter = new NewGroupAdapter(users, selectedForwardedUsers, currentUsers, isBroadcast, true, this, this);
            rvGroup.setAdapter(allUsersAdapter);
        } else {
            allUsersAdapter = new NewGroupAdapter(this.users, selectedForwardedUsers, currentUsers, isBroadcast, true, this, this);
            rvGroup.setAdapter(allUsersAdapter);
        }
    }

    @Override
    public void onSearchClose() {
        allUsersAdapter = new NewGroupAdapter(this.users, selectedForwardedUsers, currentUsers, isBroadcast, true, this, this);
        rvGroup.setAdapter(allUsersAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_new_group_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_new_group_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        init();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String groupId = getIntent().getStringExtra(IntentUtils.UID);
        isBroadcast = getIntent().getBooleanExtra(IntentUtils.IS_BROADCAST, false);

        if (isBroadcast) {
            if (groupId != null) {
                toolbarTitle.setText(getResources().getString(R.string.add_recipients));
                toolbarTitle.setText(R.string.add_recipients);
                User user = RealmHelper.getInstance().getUser(groupId);
                currentUsers = user.getBroadcast().getUsers();
                EXTRA_COUNT = currentUsers.size();
                tvAddParticipantsTvToolbar.setVisibility(View.GONE);
            } else {
                toolbarTitle.setText(getResources().getString(R.string.new_broadcast));
            }
        } else if (groupId != null) {
            User user = RealmHelper.getInstance().getUser(groupId);
            currentUsers = user.getGroup().getUsers();
            EXTRA_COUNT = currentUsers.size();
            toolbarTitle.setText(R.string.add_participants);
            tvAddParticipantsTvToolbar.setVisibility(View.GONE);
        }

        setAdapter();

        fabNext.setOnClickListener(view -> {
            if (getIntent().hasExtra(IntentUtils.UID)) {
                Intent data = new Intent();
                data.putExtra(IntentUtils.EXTRA_SELECTED_USERS, (ArrayList<? extends Parcelable>) selectedUsers);
                setResult(RESULT_OK, data);
                finish();
            } else {
                SetGroupTitleDialog dialog = new SetGroupTitleDialog(NewGroupActivity.this, "");
                if (isBroadcast) {
                    dialog.setDialogTitle(getResources().getString(R.string.broadcast_name));
                    dialog.setEditTextHint(getResources().getString(R.string.broadcast_name));
                }
                dialog.setmListener(groupTitle -> {
                    if (isBroadcast)
                        createBroadcast(groupTitle);
                    else
                        createGroup(groupTitle);
                });
                dialog.show();
            }
        });

        setSearchCallback(this);
    }

    private void createBroadcast(String broadcastName) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (NetworkHelper.isConnected(this)) {
            BroadcastManager.createNewBroadcast(broadcastName, selectedUsers, (isSuccessful, broadcastId) -> {
                progressDialog.dismiss();
                if (!isSuccessful) {
                    Toast.makeText(NewGroupActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(NewGroupActivity.this, ChatActivity.class);
                    intent.putExtra(IntentUtils.UID, broadcastId);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void createGroup(String groupTitle) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (NetworkHelper.isConnected(this)) {
            GroupManager.createNewGroup(this, groupTitle, selectedUsers, (isSuccessful, groupId) -> {
                progressDialog.dismiss();
                if (!isSuccessful) {
                    Toast.makeText(NewGroupActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(NewGroupActivity.this, ChatActivity.class);
                    intent.putExtra(IntentUtils.UID, groupId);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {
        Toolbar toolbarForward = findViewById(R.id.toolbar_forward);
        rvSelectedUsersNewGroup = findViewById(R.id.rv_selected_users_new_group);
        rvGroup = findViewById(R.id.rv_group);
        fabNext = findViewById(R.id.fab_next);
        toolbarTitle = findViewById(R.id.toolbar_title);
        tvAddParticipantsTvToolbar = findViewById(R.id.tv_add_participants_tv_toolbar);
        setSupportActionBar(toolbarForward);
        users = RealmHelper.getInstance().getListOfUsers();
        selectedUsers = new ArrayList<>();
    }

    private void setAdapter() {
        allUsersAdapter = new NewGroupAdapter(users, selectedForwardedUsers, currentUsers, isBroadcast, true, this, this);
        rvGroup.setLayoutManager(new LinearLayoutManager(this));
        rvGroup.setAdapter(allUsersAdapter);

        selectedUsersAdapter = new NewGroupSelectedUsersAdapter(selectedUsers, this, this);
        rvSelectedUsersNewGroup.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvSelectedUsersNewGroup.setAdapter(selectedUsersAdapter);
    }

    //this will called when a user clicks on a user in vertical list
    @Override
    public void onChange(User user, boolean added) {
        int count = selectedForwardedUsers.size();
        int position = selectedUsers.indexOf(user);
        if (added) {
            if (count + EXTRA_COUNT > getMaxNumberUsers()) {
                Toast.makeText(this, R.string.max_number_of_users_reached, Toast.LENGTH_SHORT).show();
            } else {
                selectedUsers.add(user);
                selectedUsersAdapter.notifyItemInserted(position);
            }
        } else {
            selectedUsers.remove(user);
            selectedUsersAdapter.notifyItemRemoved(position);
        }

        updateSelectedUsers(count);
    }

    @Override
    public void onChangeSnackBar(boolean flag) {

    }

    private int getMaxNumberUsers() {
        return isBroadcast ? MAX_BROADCAST_USERS_COUNT : MAX_GROUP_USERS_COUNT;
    }

    //this will called when a user clicks on a user in horizontal list
    @Override
    public void onRemove(User user) {
        //remove selected circle
        RecyclerView.ViewHolder viewHolderForAdapterPosition = rvGroup.findViewHolderForAdapterPosition(users.indexOf(user));
        if (viewHolderForAdapterPosition != null) {
            HidelyImageView selectedCircle = viewHolderForAdapterPosition.itemView.findViewById(R.id.img_selected);
            if (selectedCircle != null)
                selectedCircle.hide();
        }
        selectedForwardedUsers.remove(user);
        onChange(user, false);
    }


    @SuppressLint("SetTextI18n")
    private void updateSelectedUsers(int count) {
        if (count == 0) {
            tvAddParticipantsTvToolbar.setText(getResources().getString(R.string.add_participants));
            rvSelectedUsersNewGroup.setVisibility(View.GONE);
            if (fabNext.getVisibility() == View.VISIBLE)
                fabNext.hide();
        } else {
            if (fabNext.getVisibility() != View.VISIBLE)
                fabNext.show();
            if (rvSelectedUsersNewGroup.getVisibility() != View.VISIBLE) {
                rvSelectedUsersNewGroup.setVisibility(View.VISIBLE);
            }
            tvAddParticipantsTvToolbar.setText(count + " of " + getMaxNumberUsers());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

}
