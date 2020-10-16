package com.hostcart.socialbot.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.NewCallAdapter;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.PerformCall;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import io.realm.RealmResults;

public class NewCallActivity extends AppCompatActivity implements NewCallAdapter.OnClickListener {
    private RecyclerView rvNewCall;
    NewCallAdapter adapter;
    private RealmResults<User> userList;
    private boolean isInSearchMode = false;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_LIGHT)) {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_new_call_dark);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_new_call_light);
        }

        rvNewCall = findViewById(R.id.rv_new_call);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.select_contact);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userList = RealmHelper.getInstance().getListOfUsers();
        adapter = new NewCallAdapter(userList, true, this);
        rvNewCall.setLayoutManager(new LinearLayoutManager(this));
        rvNewCall.setAdapter(adapter);
        adapter.setOnUserClick(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            getMenuInflater().inflate(R.menu.menu_new_call_dark, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_new_call_light, menu);
        }
        MenuItem menuItem = menu.findItem(R.id.search_item);
        searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.trim().isEmpty()) {
                    RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, false);
                    adapter = new NewCallAdapter(users, true, NewCallActivity.this);
                    adapter.setOnUserClick(NewCallActivity.this);
                    rvNewCall.setAdapter(adapter);
                } else {
                    adapter = new NewCallAdapter(userList, true, NewCallActivity.this);
                    adapter.setOnUserClick(NewCallActivity.this);
                    rvNewCall.setAdapter(adapter);
                }
                return false;
            }
        });

        searchView.setOnCloseListener(() -> {
            isInSearchMode = false;
            adapter = new NewCallAdapter(userList, true, NewCallActivity.this);
            rvNewCall.setAdapter(adapter);
            return false;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onUserClick(View view, User user, boolean isVideo) {
        new PerformCall(NewCallActivity.this).performCall(isVideo, user.getUid());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.search_item) {
            isInSearchMode = true;

            if (searchView.isIconified())
                searchView.onActionViewExpanded();

            searchView.requestFocus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isInSearchMode)
            exitSearchMode();
        else
            super.onBackPressed();
    }

    private void exitSearchMode() {
        isInSearchMode = false;
        searchView.onActionViewCollapsed();
        adapter.notifyDataSetChanged();
    }

}
