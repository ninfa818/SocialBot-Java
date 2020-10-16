package com.hostcart.socialbot.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.AllUsersRecyclerViewAdapter;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuickAddActivity extends AppCompatActivity {

    private List<UserInfo> userInfos = new ArrayList<>();
    private AllUsersRecyclerViewAdapter allUsersRecyclerViewAdapter;

    private List<String> lst_friends = new ArrayList<>();
    private List<String> lst_request = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_quick_add_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_quick_add_light);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Quick Add");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView allUsersRecycler = findViewById(R.id.allusers_recycler);
        allUsersRecyclerViewAdapter = new AllUsersRecyclerViewAdapter(this, userInfos);
        allUsersRecycler.setAdapter(allUsersRecyclerViewAdapter);
        allUsersRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        allUsersRecyclerViewAdapter.notifyDataSetChanged();

        loadRemailList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void loadRemailList() {
        FireConstants.usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userInfos.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(dataSnapshot.getKey());
                    userInfos.add(userInfo);
                }
                onRefresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        FireConstants.friendsRef.child(Objects.requireNonNull(FireManager.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lst_friends.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    lst_friends.add(dataSnapshot.getKey());
                }
                onRefresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FireConstants.friendRequestRef.child(FireManager.getUid()).child("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lst_request.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    lst_request.add(dataSnapshot.getKey());
                }
                onRefresh();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void onRefresh() {
        if (userInfos.size() > 0) {
            if (lst_friends.size() > 0) {
                for (String uid: lst_friends) {
                    for (UserInfo userInfo: userInfos) {
                        if (userInfo.getUid().equals(uid)) {
                            userInfos.remove(userInfo);
                            break;
                        }
                    }
                }
            }
            if (lst_request.size() > 0) {
                for (String uid: lst_request) {
                    for (UserInfo userInfo: userInfos) {
                        if (userInfo.getUid().equals(uid)) {
                            userInfos.remove(userInfo);
                            break;
                        }
                    }
                }
            }
            for (UserInfo userInfo: userInfos) {
                if (userInfo.getUid().equals(FireManager.getUid())) {
                    userInfos.remove(userInfo);
                    break;
                }
            }
        }
        allUsersRecyclerViewAdapter.notifyDataSetChanged();
    }

}
