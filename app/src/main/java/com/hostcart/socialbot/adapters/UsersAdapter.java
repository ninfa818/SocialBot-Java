package com.hostcart.socialbot.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

/**
 * Created by Devlomi on 03/08/2017.
 */

//show the groupUsers from phonebook who have installed this app
public class UsersAdapter extends RealmRecyclerViewAdapter<User, RecyclerView.ViewHolder> {
    Context context;
    private List<User> userList;
    private OnItemClickListener onItemClickListener;

    public UsersAdapter(@Nullable OrderedRealmCollection<User> data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        this.userList = data;
        this.context = context;
        onItemClickListener = (OnItemClickListener) context;
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_dark, parent, false));
        } else {
            return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_user_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, int position) {
        final User user = userList.get(position);
        UserHolder mHolder = (UserHolder) holder;
        mHolder.tvName.setText(user.getUserName());
        mHolder.tvStatus.setText(user.getStatus());

        mHolder.rlltBody.setOnClickListener(view -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(user);
        });

        mHolder.userPhoto.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onUserPhotoClick(user);
        });

        loadUserPhoto(user, mHolder.userPhoto);
    }

    private void loadUserPhoto(final User user, final ImageView imageView) {
        if (user == null) return;
        if (user.getUid() == null) return;

        if (user.getThumbImg() != null) {
            byte[] bytes = BitmapUtils.encodeImageAsBytes(user.getThumbImg());
            Glide.with(context).asBitmap().load(bytes).into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout rlltBody;
        private ImageView userPhoto;
        private TextView tvName, tvStatus;

        UserHolder(View itemView) {
            super(itemView);
            rlltBody =  itemView.findViewById(R.id.container_layout);
            userPhoto = itemView.findViewById(R.id.user_photo);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
        void onUserPhotoClick(User user);
    }

}
