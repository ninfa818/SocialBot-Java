package com.hostcart.socialbot.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.FriendsRecyclerViewAdapter;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddedMeActivity extends AppCompatActivity {

    private List<UserInfo> invitedUsers = new ArrayList<>();
    private FriendsRecyclerViewAdapter friendsRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_added_me_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));

        } else {
            setContentView(R.layout.activity_added_me_light);
        }

        Objects.requireNonNull(getSupportActionBar()).setTitle("Added Me");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView placeholderText = findViewById(R.id.added_placeholder);
        RecyclerView addedmeRecyclerView = findViewById(R.id.addedme_recycler);

        loadInvitedList();

        if( invitedUsers.size() > 0 )
            placeholderText.setVisibility(View.GONE);
        else
            placeholderText.setVisibility(View.VISIBLE);

        friendsRecyclerViewAdapter = new FriendsRecyclerViewAdapter(this, invitedUsers);
        addedmeRecyclerView.setAdapter(friendsRecyclerViewAdapter);
        addedmeRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        friendsRecyclerViewAdapter.notifyDataSetChanged();
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

    private void loadInvitedList() {
        FireConstants.friendRequestRef.child(Objects.requireNonNull(FireManager.getUid()))
                .child("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                invitedUsers.clear();
                for (DataSnapshot ignored : snapshot.getChildren()) {
                    String uid = ignored.getKey();
                    FireManager.getUserInfoByUid(uid, new FireManager.userInfoListener() {
                        @Override
                        public void onFound(UserInfo userInfo) {
                            invitedUsers.add(userInfo);
                            friendsRecyclerViewAdapter.notifyDataSetChanged();
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

}
