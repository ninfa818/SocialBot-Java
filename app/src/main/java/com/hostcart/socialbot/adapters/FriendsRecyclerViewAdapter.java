package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.UserProfileActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.RealmHelper;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsRecyclerViewAdapter extends RecyclerView.Adapter<FriendsRecyclerViewAdapter.FriendsViewHolder> {

    private Context mContext;
    private FriendsRecyclerViewAdapter adapter;
    private List<UserInfo> invitedUsers;

    public FriendsRecyclerViewAdapter(Context mContext, List<UserInfo> users) {
        this.mContext = mContext;
        this.invitedUsers = users;
        adapter = this;
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new FriendsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.friend_item_dark, parent, false));
        } else {
            return new FriendsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.friend_item_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        UserInfo userInfo = invitedUsers.get(position);
        if( userInfo != null ) {
            if(userInfo.getPhoto() != null) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(Uri.parse(userInfo.getPhoto()))
                        .into(holder.friendPhotoView);
            }

            FireConstants.presenceRef.child(userInfo.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if( dataSnapshot.getValue() != null ) {
                        Object online = dataSnapshot.getValue();
                        if( online.equals("Online") )
                            holder.badgeView.setBackgroundResource(R.drawable.online_badge);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            holder.friendNameView.setText(userInfo.getName());
            holder.friendContenview.setText(userInfo.getStatus());

            holder.itemlLayout.setOnClickListener(v -> {
                AppUtils.gUid = userInfo.getUid();
                AppUtils.showOtherActivity(mContext, UserProfileActivity.class, 0);
            });

            holder.acceptButton.setOnClickListener(v -> {
                doAccept(userInfo);
            });
        }
    }

    private void doAccept(UserInfo userInfo) {
        FireConstants.friendRequestRef.child(FireManager.getUid()).child("received")
                .child(userInfo.getUid()).removeValue().addOnCompleteListener(task -> {
            FireConstants.friendRequestRef.child(userInfo.getUid()).child("sent")
                    .child(FireManager.getUid()).removeValue().addOnCompleteListener(task1 -> {
                FireConstants.usersRef.child(userInfo.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if( dataSnapshot.getValue() != null ) {
                            User user = dataSnapshot.getValue(User.class);
                            RealmHelper.getInstance().saveObjectToRealm(user);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });

                // remove user in array
                invitedUsers.remove(userInfo);
                adapter.notifyDataSetChanged();

                saveInvitedListJsonString();
                saveFriendsToFirebase(userInfo);
            });
        });
    }

    private void saveInvitedListJsonString() {
        Gson gson = new Gson();
        String jsonstring = gson.toJson(invitedUsers);
        SharedPreferencesManager.saveAddedMeListJsonString(jsonstring);
    }

    private void saveFriendsToFirebase(UserInfo userInfo) {
        FireConstants.friendsRef.child(FireManager.getUid()).child(userInfo.getUid()).setValue(userInfo.getPhone());
        FireConstants.friendsRef.child(userInfo.getUid()).child(FireManager.getUid()).setValue(FireManager.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return invitedUsers.size();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout itemlLayout;
        CircleImageView friendPhotoView;
        TextView friendNameView;
        TextView friendContenview;
        Button acceptButton;
        ImageView badgeView;

        FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemlLayout = itemView.findViewById(R.id.friend_item_layout);
            friendPhotoView = itemView.findViewById(R.id.friend_photo_image);
            friendNameView = itemView.findViewById(R.id.friend_fullname);
            friendContenview = itemView.findViewById(R.id.friend_content_text);
            acceptButton = itemView.findViewById(R.id.user_accept_button);
            badgeView = itemView.findViewById(R.id.user_badge);
        }
    }

}
