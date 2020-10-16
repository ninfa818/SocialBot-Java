package com.hostcart.socialbot.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.UserProfileActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.FireConstants;
import com.hostcart.socialbot.utils.FireManager;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUsersRecyclerViewAdapter extends RecyclerView.Adapter<AllUsersRecyclerViewAdapter.MyViewHolder> {

    private Context mContext;
    private List<UserInfo> userInfos;

    public AllUsersRecyclerViewAdapter(Context mContext, List<UserInfo> users) {
        this.mContext = mContext;
        this.userInfos = users;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.user_item_dark, parent, false));
        } else {
            return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.user_item_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        UserInfo userInfo = userInfos.get(position);
        if( userInfo != null ) {
            if( userInfo.getPhoto() != null ) {
                Glide.with(mContext)
                        .asBitmap()
                        .load(Uri.parse(userInfo.getPhoto()))
                        .into(holder.photoView);
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
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            holder.nameText.setText(userInfo.getName());
            holder.contentText.setText(userInfo.getStatus());

            holder.userLayout.setOnClickListener(v -> {
                AppUtils.gUid = userInfo.getUid();
                AppUtils.showOtherActivity(mContext, UserProfileActivity.class, 0);
            });

            //
            holder.invite.setVisibility(View.GONE);
            FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent").child(userInfo.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            holder.invite.setVisibility(View.VISIBLE);
                            if( dataSnapshot.getValue() != null ) {
                                holder.invite.setText("Added");
                            } else {
                                holder.invite.setText("Add");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

            holder.invite.setOnClickListener(v -> {
                if( holder.invite.getText().toString().equals("Add") ) {
                    holder.invite.setEnabled(false);
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(userInfo.getUid())
                            .setValue(UUID.randomUUID().toString()).addOnCompleteListener(task -> FireConstants.friendRequestRef.child(userInfo.getUid()).child("received")
                                    .child(FireManager.getUid()).setValue(UUID.randomUUID().toString()).addOnCompleteListener(task1 -> {
                                        holder.invite.setText("Added");
                                        holder.invite.setEnabled(true);
                                    }));
                } else {
                    holder.invite.setEnabled(false);
                    FireConstants.friendRequestRef.child(FireManager.getUid()).child("sent")
                            .child(userInfo.getUid()).removeValue()
                            .addOnCompleteListener(task -> FireConstants.friendRequestRef.child(userInfo.getUid()).child("received")
                                    .child(FireManager.getUid()).removeValue().addOnCompleteListener(task12 -> {
                                        holder.invite.setText("Add");
                                        holder.invite.setEnabled(true);
                                    }));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView photoView;
        TextView nameText;
        TextView contentText;
        Button invite;
        LinearLayout userLayout;
        ImageView badgeView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            photoView = itemView.findViewById(R.id.user_photo_image);
            nameText = itemView.findViewById(R.id.user_fullname);
            contentText = itemView.findViewById(R.id.user_content_text);
            invite = itemView.findViewById(R.id.user_invite_button);
            userLayout = itemView.findViewById(R.id.user_layout);
            badgeView = itemView.findViewById(R.id.user_badge);
        }
    }
}
