package com.hostcart.socialbot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.activities.ForwardActivity;
import com.hostcart.socialbot.model.UserInfo;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.AppVerUtil;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.FireManager;
import com.devlomi.hidely.hidelyviews.HidelyImageView;
import com.google.android.material.snackbar.Snackbar;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;


public class ForwardAdapter extends RealmRecyclerViewAdapter<User, RecyclerView.ViewHolder> {

    private Context context;
    private List<User> list;
    private ForwardActivity activity;
    private List<User> selectedForwardedUsers;
    private OnUserClick onUserClick;
    private List<User> currentGroupUsers;
    private boolean isAddingUsersToGroup = false;
    private boolean isBroadcast = false;

    public ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.onUserClick = onUserClick;
        activity = (ForwardActivity) context;
    }

    ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, List<User> currentGroupUsers, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.onUserClick = onUserClick;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.currentGroupUsers = currentGroupUsers;
        isAddingUsersToGroup = currentGroupUsers != null;
        activity = (ForwardActivity) context;
    }

    ForwardAdapter(@Nullable OrderedRealmCollection<User> data, List<User> selectedForwardedUsers, List<User> currentGroupUsers, boolean isBroadcast, boolean autoUpdate, Context context, OnUserClick onUserClick) {
        super(data, autoUpdate);
        this.list = data;
        this.context = context;
        this.onUserClick = onUserClick;
        this.selectedForwardedUsers = selectedForwardedUsers;
        this.currentGroupUsers = currentGroupUsers;
        this.isBroadcast = isBroadcast;
        isAddingUsersToGroup = currentGroupUsers != null;
        activity = (ForwardActivity) context;
    }


    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new ForwardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forward_dark, parent, false));
        } else {
            return new ForwardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_forward_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NotNull final RecyclerView.ViewHolder holder, int position) {
        final ForwardHolder mHolder = (ForwardHolder) holder;
        final User user = list.get(position);

        if (currentGroupUsers != null) {
            if (currentGroupUsers.contains(user)) {
                mHolder.tvDesc.setText(isBroadcast ? R.string.user_already_added_to_broadcast : R.string.user_already_added_to_group);
                mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.BOLD_ITALIC);
                mHolder.tvDesc.setTextColor(context.getColor(R.color.colorsecondary_text));
            } else {
                if (!user.isGroupBool() && !AppVerUtil.isAppSupportsGroups(user.getAppVer())) {
                    mHolder.tvDesc.setText(R.string.this_user_has_old_version);
                    mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.BOLD_ITALIC);
                    mHolder.tvDesc.setTextColor(context.getColor(R.color.colorsecondary_text));
                } else
                    setUserStatus(mHolder, user);
            }
        } else {
            setUserStatus(mHolder, user);
        }

        mHolder.rlltBody.setOnClickListener(view -> {
            if (user.isBlocked())
                Snackbar.make(activity.findViewById(android.R.id.content), R.string.user_is_blocked, Snackbar.LENGTH_SHORT).show();
            else
                itemSelected(user, mHolder);
        });

        if (selectedForwardedUsers.contains(user))
            mHolder.selectedCircle.setVisibility(View.VISIBLE);
        else
            mHolder.selectedCircle.setVisibility(View.INVISIBLE);

        loadUserPhoto(user, mHolder.userProfile);
    }

    private void setUserStatus(ForwardHolder mHolder, User user) {
        FireManager.getUserInfoByUid(user.getUid(), new FireManager.userInfoListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFound(UserInfo userInfo) {
                mHolder.tvTitle.setText(userInfo.getName() + " " + userInfo.getSurname());

                mHolder.tvDesc.setText(userInfo.getStatus());
                mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.NORMAL);
                mHolder.tvDesc.setTextColor(context.getColor(R.color.colorTextDesc));
            }

            @Override
            public void onNotFound() {
                String name = user.getUserName();
                mHolder.tvTitle.setText(name);

                mHolder.tvDesc.setText(user.getStatus() == null ? "" : user.getStatus());
                mHolder.tvDesc.setTypeface(mHolder.tvDesc.getTypeface(), Typeface.NORMAL);
                mHolder.tvDesc.setTextColor(context.getColor(R.color.colorTextDesc));
            }
        });
    }

    private void itemSelected(User user, ForwardHolder mHolder) {
        if (isAddingUsersToGroup && currentGroupUsers.contains(user) || isAddingUsersToGroup && !AppVerUtil.isAppSupportsGroups(user.getAppVer())) {
        } else if (selectedForwardedUsers.contains(user)) {
            itemRemoved(user);

            if (selectedForwardedUsers.isEmpty())
                onUserClick.onChangeSnackBar(false);
            mHolder.selectedCircle.hide();
        } else {
            mHolder.selectedCircle.show();
            itemAdded(user);
            onUserClick.onChangeSnackBar(true);
        }
    }

    private void itemRemoved(User user) {
        selectedForwardedUsers.remove(user);
        if (onUserClick != null)
            onUserClick.onChange(user, false);
    }

    private void itemAdded(User user) {
        selectedForwardedUsers.add(user);
        if (onUserClick != null)
            onUserClick.onChange(user, true);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ForwardHolder extends RecyclerView.ViewHolder {

        private RelativeLayout rlltBody;
        private ImageView userProfile;
        private TextView tvTitle;
        private EmojiconTextView tvDesc;
        private HidelyImageView selectedCircle;

        ForwardHolder(View itemView) {
            super(itemView);

            rlltBody = itemView.findViewById(R.id.container_layout);
            userProfile = itemView.findViewById(R.id.user_photo);
            tvTitle = itemView.findViewById(R.id.tv_name);
            tvDesc = itemView.findViewById(R.id.tv_status);
            selectedCircle = itemView.findViewById(R.id.img_selected);
        }
    }

    private void loadUserPhoto(final User user, final ImageView imageView) {
        if (user == null) return;
        if (user.getUid() == null) return;

        if (user.isBroadcastBool()) {
            imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic_broadcast_with_bg));
        } else if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(context).asBitmap().load(bytes).into(imageView);
        }

        if (!isBroadcast) {
            FireManager.checkAndDownloadUserPhoto(user, thumbImg -> {
                try {
                    Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(thumbImg)).into(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public List<User> getSelectedForwardedUsers() {
        return selectedForwardedUsers;
    }

    public interface OnUserClick {
        void onChange(User user, boolean added);
        void onChangeSnackBar(boolean flag);
    }

}
