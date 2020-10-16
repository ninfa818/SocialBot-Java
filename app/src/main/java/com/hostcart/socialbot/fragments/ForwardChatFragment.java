package com.hostcart.socialbot.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.ForwardActivity;
import com.hostcart.socialbot.adapters.ForwardAdapter;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.hostcart.socialbot.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import io.realm.RealmResults;


public class ForwardChatFragment extends BaseFragment {

    private ForwardActivity mActivity;
    private static final String SEPARATOR = " , ";

    private RecyclerView rvForward;
    private TextView tvSelectedContact;

    private RealmResults<User> usersList;
    public ForwardAdapter adapter;
    private ForwardChatEventListener forwardChatEventListener;


    public ForwardChatFragment(ForwardActivity activity) {
        mActivity = activity;
    }

    public interface ForwardChatEventListener {
        void onShowSnackBarEvent();
        void onHideSnackBarEvent();
        void onUpdateSelectUser(String text);
    }

    public void setForwardChatEventListener(ForwardChatEventListener forwardChatEventListener) {
        this.forwardChatEventListener = forwardChatEventListener;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            view = inflater.inflate(R.layout.fragment_forward_chat_dark, container, false);
        } else {
            view = inflater.inflate(R.layout.fragment_forward_chat_light, container, false);
        }
        initView(view);
        return view;
    }

    private void initView(View view) {
        initData();

        rvForward = view.findViewById(R.id.rv_forward);
        adapter = new ForwardAdapter(usersList, mActivity.selectedForwardedUsers, true, getContext(), new ForwardAdapter.OnUserClick() {
            @Override
            public void onChange(User user, boolean added) {
                updateSelectedUsers();
            }

            @Override
            public void onChangeSnackBar(boolean flag) {
                if (flag) {
                    showSnackbar();
                } else {
                    hideSnackbar();
                }
            }
        });
        rvForward.setLayoutManager(new LinearLayoutManager(getContext()));
        rvForward.setAdapter(adapter);

        tvSelectedContact = view.findViewById(R.id.tv_selected_contact);

        initEvent();
    }

    private void initData() {
        usersList = RealmHelper.getInstance().getForwardList();
    }

    private void initEvent() {
        mActivity.setSearchCallback(new ForwardActivity.SearchCallback() {
            @Override
            public void onQuery(String newText) {
                if (!newText.trim().isEmpty()) {
                    RealmResults<User> users = RealmHelper.getInstance().searchForUser(newText, true);
                    adapter = new ForwardAdapter(users, mActivity.selectedForwardedUsers, true, mActivity, null);
                    rvForward.setAdapter(adapter);
                } else {
                    adapter = new ForwardAdapter(usersList, mActivity.selectedForwardedUsers, true, mActivity, null);
                    rvForward.setAdapter(adapter);
                }
            }

            @Override
            public void onSearchClose() {
                adapter = new ForwardAdapter(usersList, mActivity.selectedForwardedUsers, true, mActivity, null);
                rvForward.setAdapter(adapter);
            }
        });
    }

    private void updateSelectedUsers() {
        StringBuilder userName = new StringBuilder();
        for (User user1 : adapter.getSelectedForwardedUsers()) {
            userName.append(user1.getUserName()).append(SEPARATOR);
        }

        tvSelectedContact.setText(StringUtils.removeExtraSeparators(userName.toString(), SEPARATOR));
        forwardChatEventListener.onUpdateSelectUser(StringUtils.removeExtraSeparators(userName.toString(), SEPARATOR));
    }

    private void showSnackbar() {
        forwardChatEventListener.onShowSnackBarEvent();
    }

    private void hideSnackbar() {
        tvSelectedContact.setText("");
        forwardChatEventListener.onHideSnackBarEvent();
    }

    @Override
    public boolean showAds() {
        return false;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

}
