package com.hostcart.socialbot.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.fragments.LikeUsersFragment;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LikeUsersActivity extends AppCompatActivity {

    public TabLayout tabLayout;
    public ViewPager viewPager;

    private List<Review> reviewModels = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            setContentView(R.layout.activity_like_users_dark);

            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorNew));
//            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorNew)));
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorNew));
        } else {
            setContentView(R.layout.activity_like_users_light);
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorWhite));
            window.clearFlags(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        setToolbar();
        initUIView();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }
    }

    private void initUIView() {
        tabLayout = findViewById(R.id.tap_like_user);
        viewPager = findViewById(R.id.vpg_like_user);
        tabLayout.post(() -> tabLayout.setupWithViewPager(viewPager));

        initWithData();
    }

    private void initWithData() {
        reviewModels.addAll(AppUtils.gPosts.getReviews());
        viewPager.setAdapter(new LikeUsersAdapter(getSupportFragmentManager()));
    }

    class LikeUsersAdapter extends FragmentPagerAdapter {

        private int[] check_index = {0, 0, 0, 0, 0, 0};
        private int[] type_ary = {-1, -1, -1, -1, -1, -1, -1};
        private String[] str_titles = {"Like", "Love", "Funny", "Wow", "Angry", "Sad"};
        private List<String> titles = new ArrayList<>();

        LikeUsersAdapter(FragmentManager fm) {
            super(fm);
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return new LikeUsersFragment(reviewModels, type_ary[position]);
        }

        @Override
        public int getCount() {
            int cnt = 1;
            for (Review reviewModel: reviewModels) {
                check_index[Integer.parseInt(reviewModel.getType())]++;
            }
            titles.add("All " + reviewModels.size());
            for (int i = 0; i < 6; i++) {
                int index = check_index[i];
                if (index > 0) {
                    type_ary[cnt] = i;
                    titles.add(str_titles[i] + " " + index);
                    cnt++;
                }
            }
            return cnt;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

    }

}
