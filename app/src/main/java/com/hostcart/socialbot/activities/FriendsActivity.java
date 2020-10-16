package com.hostcart.socialbot.activities;

import android.graphics.Color;
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
import com.hostcart.socialbot.adapters.MyFriendRecycleViewAdapter;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsActivity extends AppCompatActivity {

    private List<UserInfo> userList = new ArrayList<>();
    private MyFriendRecycleViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_friends_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_friends_light);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadFriendListJsonString();

        RecyclerView rvFriends = findViewById(R.id.rv_friends);
        adapter = new MyFriendRecycleViewAdapter(userList, this);
        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        adapter.notifyDataSetChanged();
    }

    private void loadFriendListJsonString() {
        FireConstants.friendsRef.child(Objects.requireNonNull(FireManager.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                            FireManager.getUserInfoByUid(dataSnapshot.getKey(), new FireManager.userInfoListener() {
                                @Override
                                public void onFound(UserInfo userInfo) {
                                    userList.add(userInfo);
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onNotFound() { }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
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

}
