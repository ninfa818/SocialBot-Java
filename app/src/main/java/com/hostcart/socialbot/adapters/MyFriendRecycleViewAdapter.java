package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.CallingActivity;
import com.hostcart.socialbot.activities.ChatActivity;
import com.hostcart.socialbot.activities.UserProfileActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.model.constants.FireCallType;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.IntentUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriendRecycleViewAdapter extends RecyclerView.Adapter<MyFriendRecycleViewAdapter.MyViewHolder> {

    private List<UserInfo> mFriendList;
    private Context mContext;
    private MyFriendRecycleViewAdapter adapter;

    public MyFriendRecycleViewAdapter(List<UserInfo> mFriendList, Context mContext) {
        this.mFriendList = mFriendList;
        this.mContext = mContext;
        adapter = this;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.myfriend_item_dark, parent, false));
        } else {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.myfriend_item_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserInfo user = mFriendList.get(position);
        if (user != null) {
            if( user.getPhoto() != null ) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(user.getPhoto())
                        .error(R.drawable.img1)
                        .into(holder.friendPhoto);
            }

            holder.friendName.setText(user.getName());
            holder.friendStatus.setText(user.getStatus());
            holder.friendDeleteImage.setOnClickListener(v -> {
                mFriendList.remove(user);
                saveFriendsListJsonString();
                adapter.notifyDataSetChanged();

                deleteFriendInFirebase(user);
            });

            holder.friendChatImage.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(IntentUtils.UID, user.getUid());
                mContext.startActivity(intent);
            });

            holder.friendCallImage.setOnClickListener(v -> {
                Intent callScreen = new Intent(mContext, CallingActivity.class);
                callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                callScreen.putExtra(IntentUtils.ISVIDEO, false);
                callScreen.putExtra(IntentUtils.UID, user.getUid());
                mContext.startActivity(callScreen);
            });

            holder.itemLayout.setOnClickListener(v -> FireManager.getUserInfoByUid(user.getUid(), new FireManager.userInfoListener() {
                @Override
                public void onFound(UserInfo userInfo) {
                    AppUtils.gUid = userInfo.getUid();
                    AppUtils.showOtherActivity(mContext, UserProfileActivity.class, 0);
                }

                @Override
                public void onNotFound() {

                }
            }));
        }
    }

    private void saveFriendsListJsonString() {
        Gson gson = new Gson();
        String jsonstring = gson.toJson(mFriendList);
        SharedPreferencesManager.saveFriendsListJsonString(jsonstring);
    }

    private void deleteFriendInFirebase( UserInfo user ) {
        FireConstants.friendsRef.child(FireManager.getUid()).child(user.getUid()).removeValue();
    }

    @Override
    public int getItemCount() {
        return mFriendList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView friendPhoto;
        TextView friendName;
        TextView friendStatus;
        ImageView friendDeleteImage;
        ImageView friendChatImage;
        ImageView friendCallImage;
        RelativeLayout itemLayout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            friendPhoto = itemView.findViewById(R.id.friend_photo_image);
            friendName = itemView.findViewById(R.id.friend_fullname);
            friendStatus = itemView.findViewById(R.id.friend_status);
            friendDeleteImage = itemView.findViewById(R.id.friend_delete_image);
            friendChatImage = itemView.findViewById(R.id.friend_chat_image);
            friendCallImage = itemView.findViewById(R.id.friend_call_image);
            itemLayout = itemView.findViewById(R.id.friend_item_layout);
        }
    }

}
