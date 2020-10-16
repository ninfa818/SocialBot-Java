package com.hostcart.socialbot.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hostcart.socialbot.R;
import com.hostcart.socialbot.model.realms.User;
import com.hostcart.socialbot.utils.AppUtils;
import com.hostcart.socialbot.utils.BitmapUtils;
import com.hostcart.socialbot.utils.SharedPreferencesManager;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class NewCallAdapter extends RealmRecyclerViewAdapter<User, NewCallAdapter.UserHolder> {

    private List<User> userList;
    private Context context;
    private OnClickListener onUserClick;

    public NewCallAdapter(@Nullable OrderedRealmCollection data, boolean autoUpdate, Context context) {
        super(data, autoUpdate);
        userList = data;
        this.context = context;
    }


    public void setOnUserClick(OnClickListener onUserClick) {
        this.onUserClick = onUserClick;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SharedPreferencesManager.getThemeMode().equals(AppUtils.THEME_DARK)) {
            return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_new_call_dark, parent, false));
        } else {
            return new UserHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_new_call_light, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.bind(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserHolder extends RecyclerView.ViewHolder {
        private CircleImageView profileImage;
        private TextView tvUsername;
        private ImageButton btnCall;
        private ImageButton btnVideoCall;

        UserHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            tvUsername = itemView.findViewById(R.id.tv_username);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnVideoCall = itemView.findViewById(R.id.btn_video_call);
        }

        public void bind(final User user) {
            tvUsername.setText(user.getUserName());
            btnCall.setOnClickListener(view -> {
                if (onUserClick != null)
                    onUserClick.onUserClick(view, user,false);
            });

            btnVideoCall.setOnClickListener(view -> {
                if (onUserClick != null)
                    onUserClick.onUserClick(view, user,true);
            });

            String imgUrl = user.getThumbImg();
            if (imgUrl == null) {
                imgUrl = "";
            }
            Glide.with(context).asBitmap().load(BitmapUtils.encodeImageAsBytes(imgUrl)).into(profileImage);
        }
    }

    public interface OnClickListener {
        void onUserClick(View view, User user, boolean isVideo);
    }

}
