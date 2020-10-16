package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.CallingActivity;
import com.hostcart.socialbot.activities.ChatActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.model.constants.FireCallType;
import com.hostcart.socialbot.utils.IntentUtils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendChatRecyclerViewAdapter extends RecyclerView.Adapter<FriendChatRecyclerViewAdapter.MyViewHolder> {

    private int view_type = 0;

    private Context mContext;
    private List<UserInfo> frienList;

    public FriendChatRecyclerViewAdapter(Context mContext, List<UserInfo> frienList, int type) {
        this.mContext = mContext;
        this.frienList = frienList;
        this.view_type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_chat_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserInfo userInfo = frienList.get(position);
        if( userInfo != null ) {
            Glide.with(mContext)
                    .asBitmap()
                    .load(Uri.parse(userInfo.getPhoto()))
                    .into(holder.friendPhoto);
            holder.friendName.setText(userInfo.getName());
            holder.friendStatus.setText(userInfo.getStatus());

            holder.itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    switch (view_type) {
                        case 0:// chat
                            Intent intent = new Intent(mContext, ChatActivity.class);
                            intent.putExtra(IntentUtils.UID, userInfo.getUid());
                            mContext.startActivity(intent);
                            break;
                        case 1:// call
                            Intent callScreen = new Intent(mContext, CallingActivity.class);
                            callScreen.putExtra(IntentUtils.PHONE_CALL_TYPE, FireCallType.OUTGOING);
                            callScreen.putExtra(IntentUtils.ISVIDEO, false);
                            callScreen.putExtra(IntentUtils.UID, userInfo.getUid());
                            mContext.startActivity(callScreen);
                            break;
                        case 2:// post
                            break;
                    }
                    // chat activity
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return frienList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout itemLayout;
        CircleImageView friendPhoto;
        TextView friendName;
        TextView friendStatus;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            itemLayout = itemView.findViewById(R.id.friend_item_layout);
            friendPhoto = itemView.findViewById(R.id.friend_photo_image);
            friendName = itemView.findViewById(R.id.friend_fullname);
            friendStatus = itemView.findViewById(R.id.friend_status);
        }
    }
}
