package com.hostcart.socialbot.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.AddUserAdapter;
import com.hostcart.socialbot.model.Review;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.FireManager;

import java.util.ArrayList;
import java.util.List;

public class LikeUsersFragment extends Fragment {

    private int mType;

    private List<Review> reviewModels;
    private List<UserInfo> userModels = new ArrayList<>();

    public LikeUsersFragment(List<Review> reviewModels, int type) {
        mType = type;
        this.reviewModels = reviewModels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_like_user, container, false);

        initWithView(fragment);

        return fragment;
    }

    private void initWithView(View fragment) {
        ListView lst_list = fragment.findViewById(R.id.lst_like_users);
        AddUserAdapter usersAdapter = new AddUserAdapter(getContext(), userModels);
        lst_list.setAdapter(usersAdapter);

        userModels.clear();
        for (Review reviewModel: reviewModels) {
            if (mType == -1) {
                FireManager.getUserInfoByUid(reviewModel.getUserid(), new FireManager.userInfoListener() {
                    @Override
                    public void onFound(UserInfo userInfo) {
                        userModels.add(userInfo);
                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNotFound() {
                        Toast.makeText(getContext(), R.string.normal_server_error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                if (Integer.parseInt(reviewModel.getType()) == mType) {
                    FireManager.getUserInfoByUid(reviewModel.getUserid(), new FireManager.userInfoListener() {
                        @Override
                        public void onFound(UserInfo userInfo) {
                            userModels.add(userInfo);
                            usersAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onNotFound() {
                            Toast.makeText(getContext(), R.string.normal_server_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

}
