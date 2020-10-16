package com.hostcart.socialbot.utils;

import com.hostcart.socialbot.model.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class UserInfoManager {

    private List<String> addedMeUids;
    private List<String> friendsUids;

    private List<UserInfo> remainUserInfos;
    private List<UserInfo> addedMeUserInfos;
    private List<UserInfo> friendUserInfos;

    public UserInfoManager() {
        initUids();
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

    public void loadAddedMeList() {
        FriendsManager.getAddedMeList(FireManager.getUid(), new FriendsManager.OnUserIdListener() {
            @Override
            public void onFound(List<String> uids) {
                addedMeUids.addAll(uids);

                for( String uid : uids ) {

                    FriendsManager.getAddedMeUser(uid, new FriendsManager.OnUserListener() {
                        @Override
                        public void onFound(UserInfo userInfo) {
                            addedMeUserInfos.add(userInfo);

                            if( uid.equals(uids.get(uids.size()-1)) )
                                saveInvitedListJsonString();

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

                saveRemainListJsonString();

            }

            @Override
            public void onNotFound() {

            }
        });
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
}
