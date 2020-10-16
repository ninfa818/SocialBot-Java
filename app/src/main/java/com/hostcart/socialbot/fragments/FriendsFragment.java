package com.hostcart.socialbot.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hostcart.socialbot.R;
import com.hostcart.socialbot.adapters.AllUsersRecyclerViewAdapter;
import com.hostcart.socialbot.adapters.FriendsRecyclerViewAdapter;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.FriendsManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends BaseFragment {

    private RecyclerView allUsersRecycler;
    private RecyclerView friendsRecycler;

    private AllUsersRecyclerViewAdapter allUsersRecyclerViewAdapter;
    private FriendsRecyclerViewAdapter friendsRecyclerViewAdapter;

    private TextView placeholderView;
    private TextView addmeView;
    private RelativeLayout quickaddLayout;

    private boolean isFriendClick = false;
    private boolean isQuickClick = false;

    private List<String> addedMeUids;
    private List<String> friendsUids;

    private List<UserInfo> remainUserInfos;
    private List<UserInfo> addedMeUserInfos;
    private List<UserInfo> friendUserInfos;

    private LinearLayout addedmeLayout;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        allUsersRecycler = view.findViewById(R.id.allusers_recycler);
        friendsRecycler = view.findViewById(R.id.friends_recycler);

        addedmeLayout = view.findViewById(R.id.addedme_layout);
        quickaddLayout = view.findViewById(R.id.quickadd_layout);
        addedmeLayout.setVisibility(View.GONE);
        quickaddLayout.setVisibility(View.GONE);

        addmeView = view.findViewById(R.id.addme_text);
        addmeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFriendClick = !isFriendClick;
                if( isFriendClick )
                    friendsRecycler.setVisibility(View.GONE);
                else friendsRecycler.setVisibility(View.VISIBLE);
            }
        });


        initUids();
        loadAddedMeList();

        //
        friendsRecyclerViewAdapter = new FriendsRecyclerViewAdapter(getActivity(), addedMeUserInfos);
        friendsRecycler.setAdapter(friendsRecyclerViewAdapter);
        friendsRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        friendsRecyclerViewAdapter.notifyDataSetChanged();

        // set invite
        allUsersRecyclerViewAdapter = new AllUsersRecyclerViewAdapter(getActivity(), remainUserInfos);
        allUsersRecycler.setAdapter(allUsersRecyclerViewAdapter);
        allUsersRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        allUsersRecyclerViewAdapter.notifyDataSetChanged();

        return view;
    }

    private void saveFriendsListJsonString() {
        Gson gson = new Gson();
        String jsonstring = gson.toJson(friendUserInfos);

        SharedPreferencesManager.saveFriendsListJsonString(jsonstring);
    }

    private boolean loadFriendListJsonString() {
        String jsonstring = SharedPreferencesManager.getFriendsListJsonString();
        if( jsonstring == null || jsonstring.equals("") )
            return false;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        friendUserInfos = gson.fromJson(jsonstring, token.getType());
        return true;
    }

    private void saveRemainListJsonString() {
        Gson gson = new Gson();
        String jsonstring = gson.toJson(remainUserInfos);

        SharedPreferencesManager.saveRemailListJsonString(jsonstring);
    }

    private boolean loadRemainList() {
        String jsonstring = SharedPreferencesManager.getRemainListJsonString();
        if( jsonstring == null || jsonstring.equals("") )
            return false;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        remainUserInfos = gson.fromJson(jsonstring, token.getType());
        return true;
    }

    private void saveInvitedListJsonString() {
        Gson gson = new Gson();
        String jsonstring = gson.toJson(addedMeUserInfos);

        SharedPreferencesManager.saveAddedMeListJsonString(jsonstring);
    }

    private boolean loadInvitedList() {
        String jsonstring = SharedPreferencesManager.getAddedMeListJsonString();
        if( jsonstring == null || jsonstring.equals("") )
            return false;

        Gson gson = new Gson();
        TypeToken<List<UserInfo>> token = new TypeToken<List<UserInfo>>() {};
        addedMeUserInfos = gson.fromJson(jsonstring, token.getType());
        return true;
    }

    private void initUids() {

        if( addedMeUids == null )
            addedMeUids = new ArrayList<>();
        else
            addedMeUids.clear();

        if( friendsUids == null )
            friendsUids = new ArrayList<>();
        else
            friendsUids.clear();

        if( remainUserInfos == null )
            remainUserInfos = new ArrayList<>();
        else
            remainUserInfos.clear();

        if( addedMeUserInfos == null )
            addedMeUserInfos = new ArrayList<>();
        else
            addedMeUserInfos.clear();

        if( friendUserInfos == null )
            friendUserInfos = new ArrayList<>();
        else
            friendUserInfos.clear();
    }

    private void loadAddedMeList() {
        FriendsManager.getAddedMeList(FireManager.getUid(), new FriendsManager.OnUserIdListener() {
            @Override
            public void onFound(List<String> uids) {
                addedMeUids.addAll(uids);

                for( String uid : uids ) {

                    FriendsManager.getAddedMeUser(uid, new FriendsManager.OnUserListener() {
                        @Override
                        public void onFound(UserInfo userInfo) {
                            addedMeUserInfos.add(userInfo);

//                            friendsRecyclerViewAdapter.notifyDataSetChanged();

                            if( uid.equals(uids.get(uids.size()-1)) )
                                saveInvitedListJsonString();

                            //
//                            if( addedmeLayout.getVisibility() == View.GONE )
//                                addedmeLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onNotFound() {

                        }
                    });

                }

                loadFriendsList();
            }

            @Override
            public void onNotFound() {
                loadFriendsList();
            }
        });
    }

    private void loadFriendsList() {
        FriendsManager.getFriendsList(FireManager.getUid(), new FriendsManager.OnUserIdListener() {
            @Override
            public void onFound(List<String> uids) {
                friendsUids.addAll(uids);

                for( String uid : uids ) {

                    FriendsManager.getAddedMeUser(uid, new FriendsManager.OnUserListener() {
                        @Override
                        public void onFound(UserInfo userInfo) {
                            friendUserInfos.add(userInfo);

                            if( uid.equals(uids.get(uids.size()-1)) )
                                saveFriendsListJsonString();

                        }

                        @Override
                        public void onNotFound() {

                        }
                    });

                }

                loadRemainUsers();
            }

            @Override
            public void onNotFound() {
                loadRemainUsers();
            }
        });
    }

    private void loadRemainUsers() {
        FriendsManager.getRemainUsers(addedMeUids, friendsUids, new FriendsManager.OnUserListener() {
            @Override
            public void onFound(UserInfo userInfo) {
                remainUserInfos.add(userInfo);

//                allUsersRecyclerViewAdapter.notifyDataSetChanged();

                saveRemainListJsonString();

//                if( quickaddLayout.getVisibility() == View.GONE )
//                    quickaddLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNotFound() {

            }
        });
    }

    @Override
    public boolean showAds() {
        return getResources().getBoolean(R.bool.is_calls_ad_enabled);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);


    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }
}
