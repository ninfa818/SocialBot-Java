package com.hostcart.socialbot.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hostcart.socialbot.model.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendsManager {

    public static List<UserInfo> addedMeUsers = new ArrayList<>();
    public static List<UserInfo> quickAddUsers = new ArrayList<>();

    public static void getAddedMeList( String uid, final OnUserIdListener listener ) {
        List<String> uids = new ArrayList<>();

        FireConstants.friendRequestRef.child(uid).child("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    for( DataSnapshot dataSnapshot1 : dataSnapshot.getChildren() ) {
                        String key = dataSnapshot1.getKey();
                        uids.add(key);
                    }
                    listener.onFound(uids);
                } else
                    listener.onNotFound();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getAddedMeUser( String uid, final OnUserListener listener ) {
        FireConstants.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    String uid = dataSnapshot.getKey();
                    UserInfo userInfo = dataSnapshot.getValue(UserInfo.class);
                    userInfo.setUid(uid);
                    listener.onFound(userInfo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getFriendsList( String uid, final OnUserIdListener listener ) {
        List<String> uids = new ArrayList<>();

        FireConstants.friendsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    for( DataSnapshot dataSnapshot1 : dataSnapshot.getChildren() ) {
                        String key = dataSnapshot1.getKey();
                        uids.add(key);
                    }
                    listener.onFound(uids);
                } else
                    listener.onNotFound();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getRemainUsers( List<String> addedMeUids, List<String> friendsUids, final OnUserListener listener ) {

        FireConstants.usersRef.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if( dataSnapshot.getValue() != null ) {

                    String key = dataSnapshot.getKey();
                    UserInfo user = dataSnapshot.getValue(UserInfo.class);

                    if( addedMeUids.contains(key) )
                        return;

                    if( friendsUids.contains(key) )
                        return;

                    if( FireManager.getUid().equals(key) )
                        return;

                    user.setUid(key);
                    listener.onFound(user);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void getAddedMeUserIds() {

        FireConstants.friendRequestRef.child(FireManager.getUid()).child("received").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    for( DataSnapshot dataSnapshot1 : dataSnapshot.getChildren() ) {
                        String userId = dataSnapshot1.getKey();

                        FireManager.getUserInfoByUid(userId, new FireManager.userInfoListener() {
                            @Override
                            public void onFound(UserInfo userInfo) {

                            }

                            @Override
                            public void onNotFound() {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public interface OnUserIdListener {
        void onFound( List<String> uids );

        void onNotFound();
    }

    public interface OnUserListener {
        void onFound( UserInfo userInfo );

        void onNotFound();
    }

    public interface OnUsersListener {
        void onFound(List<UserInfo> userInfos);

        void onNotFound();
    }
}
